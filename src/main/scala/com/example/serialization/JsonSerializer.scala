package com.example.serialization

import com.example.models.{User, Product, ProductStatus}


object JsonSerializer {
  
  
  def toJson(user: User): String = {
    s"""{
      |  "id": ${user.id},
      |  "name": "${escapeJson(user.name)}",
      |  "email": "${escapeJson(user.email)}",
      |  "age": ${user.age},
      |  "isActive": ${user.isActive}
      |}""".stripMargin
  }
  
  
  def toJson(product: Product): String = {
    s"""{
      |  "id": ${product.id},
      |  "name": "${escapeJson(product.name)}",
      |  "price": ${product.price},
      |  "quantity": ${product.quantity},
      |  "status": "${product.status}",
      |  "createdAt": "${product.createdAt}"
      |}""".stripMargin
  }
  
  
  def toJsonArray[T](items: List[T], serializer: T => String): String = {
    val itemsJson = items.map(serializer).mkString(",\n    ")
    s"[\n    $itemsJson\n  ]"
  }
  
  
  def fromJsonUser(json: String): Option[User] = {
    try {
      val id = extractLong(json, "id")
      val name = extractString(json, "name")
      val email = extractString(json, "email")
      val age = extractInt(json, "age")
      val isActive = extractBoolean(json, "isActive").getOrElse(true)
      
      User.create(id, name, email, age).map { user =>
        if (isActive) user else user.deactivate
      }
    } catch {
      case _: Exception => None
    }
  }
  
  
  def fromJsonProduct(json: String): Option[Product] = {
    try {
      val name = extractString(json, "name")
      val price = extractBigDecimal(json, "price")
      val quantity = extractInt(json, "quantity")
      Product.create(name, price, quantity)
    } catch {
      case _: Exception => None
    }
  }
  
  private def escapeJson(str: String): String = {
    str.replace("\\", "\\\\")
       .replace("\"", "\\\"")
       .replace("\n", "\\n")
       .replace("\r", "\\r")
       .replace("\t", "\\t")
  }
  
  private def extractString(json: String, key: String): String = {
    val pattern = s""""$key"\\s*:\\s*"([^"]+)"""".r
    pattern.findFirstMatchIn(json).map(_.group(1)).getOrElse("")
  }
  
  private def extractLong(json: String, key: String): Long = {
    val pattern = s""""$key"\\s*:\\s*(\\d+)""".r
    pattern.findFirstMatchIn(json).map(_.group(1).toLong).getOrElse(0L)
  }
  
  private def extractInt(json: String, key: String): Int = {
    val pattern = s""""$key"\\s*:\\s*(\\d+)""".r
    pattern.findFirstMatchIn(json).map(_.group(1).toInt).getOrElse(0)
  }
  
  private def extractBigDecimal(json: String, key: String): BigDecimal = {
    val pattern = s""""$key"\\s*:\\s*(\\d+(?:\\.\\d+)?)""".r
    pattern.findFirstMatchIn(json).map(m => BigDecimal(m.group(1))).getOrElse(BigDecimal(0))
  }
  
  private def extractBoolean(json: String, key: String): Option[Boolean] = {
    val pattern = s""""$key"\\s*:\\s*(true|false)""".r
    pattern.findFirstMatchIn(json).map(_.group(1).toBoolean)
  }
}

