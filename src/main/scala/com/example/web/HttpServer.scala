package com.example.web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future

import com.example.service.{UserService, ProductService}

object HttpServer {
  def start(host: String = "0.0.0.0", port: Int = 8080): Future[Http.ServerBinding] = {
    implicit val system: ActorSystem = ActorSystem("scala-project-server")
    implicit val ec: ExecutionContextExecutor = system.dispatcher
    implicit val mat: Materializer = Materializer(system)

    val userService = new UserService()
    val productService = new ProductService()
    val routes = new Routes(userService, productService)(ec).route

    Http().newServerAt(host, port).bind(routes)
  }
}
