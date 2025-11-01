package com.example.models

import java.time.LocalDateTime

/** Статус продукта */
sealed trait ProductStatus
case object Active extends ProductStatus
case object Discontinued extends ProductStatus
case object OutOfStock extends ProductStatus

/**
 * Модель продукта в системе
 * 
 * @param id уникальный идентификатор продукта
 * @param name название продукта
 * @param price цена продукта (должна быть > 0)
 * @param quantity количество на складе (>= 0)
 * @param status статус продукта
 * @param createdAt дата и время создания записи
 */
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
  
  def applyDiscount(percentage: BigDecimal): Product = {
    if (percentage > 0 && percentage <= 100) {
      val discount = price * (percentage / 100)
      copy(price = price - discount)
    } else {
      this
    }
  }
  
  def isExpensive(threshold: BigDecimal): Boolean = price > threshold
  
  def isLowStock(minStock: Int): Boolean = quantity < minStock
  
  def canSell(amount: Int): Boolean = isAvailable && quantity >= amount
  
  def sell(amount: Int): Option[Product] = {
    if (canSell(amount)) {
      val newQuantity = quantity - amount
      val newStatus = if (newQuantity == 0) OutOfStock else status
      Some(copy(quantity = newQuantity, status = newStatus))
    } else {
      None
    }
  }
  
  def restock(amount: Int): Product = {
    if (amount > 0) {
      val newQuantity = quantity + amount
      val newStatus = if (status == OutOfStock && newQuantity > 0) Active else status
      copy(quantity = newQuantity, status = newStatus)
    } else {
      this
    }
  }
  
  def daysSinceCreation: Long = {
    java.time.Duration.between(createdAt, LocalDateTime.now()).toDays
  }
}

/**
 * Компаньон-объект для Product с фабричными методами
 */
object Product {
  /**
   * Создает новый продукт с валидацией
   * 
   * @param name название продукта
   * @param price цена продукта
   * @param quantity количество на складе
   * @return Some(Product) если данные валидны, None иначе
   */
  def create(name: String, price: BigDecimal, quantity: Int): Option[Product] = {
    if (price > 0 && quantity >= 0) {
      Some(Product(0, name, price, quantity, Active))
    } else {
      None
    }
  }
}

