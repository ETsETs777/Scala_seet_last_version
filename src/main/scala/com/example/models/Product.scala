package com.example.models

import java.time.LocalDateTime

/**
 * Модель продукта с sealed trait для безопасного паттерн-матчинга
 */
sealed trait ProductStatus
case object Active extends ProductStatus
case object Discontinued extends ProductStatus
case object OutOfStock extends ProductStatus

case class Product(
  id: Long,
  name: String,
  price: BigDecimal,
  quantity: Int,
  status: ProductStatus,
  createdAt: LocalDateTime = LocalDateTime.now()
) {
  def isAvailable: Boolean = status == Active && quantity > 0
  
  def totalValue: BigDecimal = price * quantity
  
  def updatePrice(newPrice: BigDecimal): Product = {
    if (newPrice > 0) copy(price = newPrice) else this
  }
}

object Product {
  def create(name: String, price: BigDecimal, quantity: Int): Option[Product] = {
    if (price > 0 && quantity >= 0) {
      Some(Product(0, name, price, quantity, Active))
    } else {
      None
    }
  }
}

