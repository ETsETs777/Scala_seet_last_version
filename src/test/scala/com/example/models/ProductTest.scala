package com.example.models

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.math.BigDecimal

class ProductTest extends AnyFlatSpec with Matchers {
  
  "Product" should "create valid product" in {
    val product = Product.create("Test Product", BigDecimal(100), 10)
    product should be(Some(Product(0, "Test Product", BigDecimal(100), 10, Active)))
  }
  
  it should "reject invalid price" in {
    Product.create("Test", BigDecimal(-10), 5) should be(None)
    Product.create("Test", BigDecimal(0), 5) should be(None)
  }
  
  it should "reject negative quantity" in {
    Product.create("Test", BigDecimal(10), -1) should be(None)
  }
  
  it should "check availability correctly" in {
    val available = Product(1, "Item", BigDecimal(10), 5, Active)
    val unavailable = Product(2, "Item", BigDecimal(10), 0, Active)
    val discontinued = Product(3, "Item", BigDecimal(10), 5, Discontinued)
    
    available.isAvailable should be(true)
    unavailable.isAvailable should be(false)
    discontinued.isAvailable should be(false)
  }
  
  it should "calculate total value correctly" in {
    val product = Product(1, "Item", BigDecimal(10.50), 3, Active)
    product.totalValue should be(BigDecimal(31.50))
  }
  
  it should "apply discount correctly" in {
    val product = Product(1, "Item", BigDecimal(100), 1, Active)
    val discounted = product.applyDiscount(20)
    discounted.price should be(BigDecimal(80))
  }
  
  it should "check if expensive" in {
    val expensive = Product(1, "Item", BigDecimal(1000), 1, Active)
    val cheap = Product(2, "Item", BigDecimal(50), 1, Active)
    
    expensive.isExpensive(BigDecimal(500)) should be(true)
    cheap.isExpensive(BigDecimal(500)) should be(false)
  }
  
  it should "check low stock" in {
    val lowStock = Product(1, "Item", BigDecimal(10), 2, Active)
    val normalStock = Product(2, "Item", BigDecimal(10), 10, Active)
    
    lowStock.isLowStock(5) should be(true)
    normalStock.isLowStock(5) should be(false)
  }
  
  it should "sell product correctly" in {
    val product = Product(1, "Item", BigDecimal(10), 10, Active)
    val sold = product.sell(3)
    
    sold should be(Some(Product(1, "Item", BigDecimal(10), 7, Active)))
  }
  
  it should "not sell if insufficient stock" in {
    val product = Product(1, "Item", BigDecimal(10), 5, Active)
    product.sell(10) should be(None)
  }
  
  it should "restock product correctly" in {
    val product = Product(1, "Item", BigDecimal(10), 5, Active)
    val restocked = product.restock(10)
    
    restocked.quantity should be(15)
  }
}

