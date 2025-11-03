package com.example.service

import org.scalatest.funsuite.AnyFunSuite
import com.example.models.{Product, Active}

class ProductServiceValidationTest extends AnyFunSuite {
  test("addProduct rejects invalid price and quantity") {
    val service = new ProductService()
    val pBadPrice = Product(0, "P", BigDecimal(0), 1, Active)
    intercept[IllegalArgumentException] { service.addProduct(pBadPrice).get }

    val pBadQty = Product(0, "P", BigDecimal(10), -1, Active)
    intercept[IllegalArgumentException] { service.addProduct(pBadQty).get }
  }

  test("addProduct enforces rate limit") {
    val service = new ProductService()
    var thrown = false
    try {
      (1 to 250).foreach { i =>
        val p = Product(0, s"P$i", BigDecimal(10 + i), 1, Active)
        service.addProduct(p)
      }
    } catch {
      case _: IllegalStateException => thrown = true
    }
    assert(thrown)
  }
}
