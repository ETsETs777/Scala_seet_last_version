package com.example.web

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.model.HttpMethods
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

import com.example.service.{UserService, ProductService}
import com.example.models.{User, Product, Active}
import com.example.metrics.GlobalMetrics
import com.example.util.FileService
import com.example.util.Localization
import com.example.interop.{GlobalScriptExecutor, Python, JavaScript, Bash}

object JsonUtil {
  def jsonResponse(body: String): HttpEntity.Strict = HttpEntity(ContentTypes.`application/json`, body)
  def textResponse(body: String): HttpEntity.Strict = HttpEntity(ContentTypes.`text/plain(UTF-8)`, body)

  def userToJson(u: User): String = {
    s"""{"id":${u.id},"name":"${escape(u.name)}","email":"${escape(u.email)}","age":${u.age},"isActive":${u.isActive}}"""
  }
  def productToJson(p: Product): String = {
    s"""{"id":${p.id},"name":"${escape(p.name)}","price":${p.price},"quantity":${p.quantity},"status":"${p.status}"}"""
  }
  def listJson(items: List[String]): String = items.mkString("[", ",", "]")

  private def escape(s: String): String = s.replace("\\", "\\\\").replace("\"", "\\\"")
}

class Routes(userService: UserService, productService: ProductService)(implicit ec: ExecutionContext) {
  import JsonUtil._

  val route: Route =
    pathPrefix("api") {
      concat(
        pathPrefix("users") {
          concat(
            get {
              parameter("q".?) { q =>
                val users = q.map(userService.searchUsers).getOrElse(userService.getAllUsers)
                complete(jsonResponse(listJson(users.map(userToJson))))
              }
            },
            post {
              entity(as[String]) { body =>
                val id = extractLong(body, "id").getOrElse(0L)
                val name = extractString(body, "name").getOrElse("")
                val email = extractString(body, "email").getOrElse("")
                val age = extractInt(body, "age").getOrElse(0)
                User.create(id, name, email, age) match {
                  case Some(u) =>
                    userService.addUser(u)
                    complete(StatusCodes.Created, jsonResponse(userToJson(u)))
                  case None => complete(StatusCodes.BadRequest, textResponse("Invalid user"))
                }
              }
            }
          )
        },
        pathPrefix("products") {
          concat(
            get {
              parameter("q".?) { q =>
                val products = q.map(productService.searchProductsByName).getOrElse(productService.getAllProducts)
                complete(jsonResponse(listJson(products.map(productToJson))))
              }
            },
            post {
              entity(as[String]) { body =>
                val name = extractString(body, "name").getOrElse("")
                val price = extractBigDecimal(body, "price").getOrElse(BigDecimal(0))
                val qty = extractInt(body, "quantity").getOrElse(0)
                Product.create(name, price, qty) match {
                  case Some(p) =>
                    val added = productService.addProduct(p).get
                    complete(StatusCodes.Created, jsonResponse(productToJson(added)))
                  case None => complete(StatusCodes.BadRequest, textResponse("Invalid product"))
                }
              }
            }
          )
        },
        path("metrics") {
          get {
            val data = GlobalMetrics.exportPrometheus
            complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, data))
          }
        },
        path("health") {
          get {
            optionalHeaderValueByName("Accept-Language") { langHeader =>
              val lang = Localization.detect(langHeader)
              val body = s"{\"status\":\"${Localization.t(lang, "health.ok")}\"}"
              complete(HttpEntity(ContentTypes.`application/json`, body))
            }
          }
        },
        path("api" / "execute") {
          post {
            entity(as[String]) { body =>
              try {
                val lang = extractString(body, "language").getOrElse("python")
                val code = extractString(body, "code").getOrElse("")
                val language = lang match {
                  case "python" => Python
                  case "javascript" | "js" => JavaScript
                  case "bash" | "sh" => Bash
                  case _ => Python
                }
                val result = GlobalScriptExecutor.executeCode(language, code)
                val response = result.fold(
                  err => s"""{"success":false,"error":"${escapeJson(err.getMessage)}"}""",
                  output => s"""{"success":true,"output":"${escapeJson(output)}"}"""
                )
                complete(jsonResponse(response))
              } catch {
                case e: Exception =>
                  complete(StatusCodes.BadRequest, textResponse(s"Error: ${e.getMessage}"))
              }
            }
          }
        }
      )
    }

  private def extractString(json: String, key: String): Option[String] = {
    val pattern = ("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"").r
    pattern.findFirstMatchIn(json).map(_.group(1))
  }
  private def extractLong(json: String, key: String): Option[Long] = {
    val pattern = ("\"" + key + "\"\\s*:\\s*(\\d+)").r
    pattern.findFirstMatchIn(json).map(_.group(1).toLong)
  }
  private def extractInt(json: String, key: String): Option[Int] = {
    val pattern = ("\"" + key + "\"\\s*:\\s*(\\d+)").r
    pattern.findFirstMatchIn(json).map(_.group(1).toInt)
  }
  private def extractBigDecimal(json: String, key: String): Option[BigDecimal] = {
    val pattern = ("\"" + key + "\"\\s*:\\s*(\\d+(?:\\.\\d+)?)").r
    pattern.findFirstMatchIn(json).map(m => BigDecimal(m.group(1)))
  }
  
  private def escapeJson(s: String): String = {
    s.replace("\\", "\\\\")
     .replace("\"", "\\\"")
     .replace("\n", "\\n")
     .replace("\r", "\\r")
     .replace("\t", "\\t")
  }
}
