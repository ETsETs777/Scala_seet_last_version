package com.example.service

import com.example.models.{Product, ProductStatus, Active, OutOfStock}
import scala.util.Try

/**
 * Сервис для управления продуктами
 * 
 * Предоставляет операции по управлению складом и продажами
 */
class ProductService {
  private var products: Map[Long, Product] = Map.empty
  private var nextId: Long = 1
  
  def addProduct(product: Product): Try[Product] = Try {
    val productWithId = product.copy(id = nextId)
    products = products + (nextId -> productWithId)
    nextId += 1
    productWithId
  }
  
  def getProduct(id: Long): Option[Product] = products.get(id)
  
  def getAllProducts: List[Product] = products.values.toList
  
  def getAvailableProducts: List[Product] = {
    products.values.filter(_.isAvailable).toList
  }
  
  def getProductsByStatus(status: ProductStatus): List[Product] = {
    products.values.filter(_.status == status).toList
  }
  
  def updateProductStatus(id: Long, status: ProductStatus): Option[Product] = {
    products.get(id).map { product =>
      val updated = product.copy(status = status)
      products = products + (id -> updated)
      updated
    }
  }
  
  def restockProduct(id: Long, quantity: Int): Option[Product] = {
    products.get(id).flatMap { product =>
      if (quantity > 0) {
        val updated = product.copy(
          quantity = product.quantity + quantity,
          status = if (product.status == OutOfStock) Active else product.status
        )
        products = products + (id -> updated)
        Some(updated)
      } else {
        None
      }
    }
  }
  
  def sellProduct(id: Long, quantity: Int): Option[Product] = {
    products.get(id).flatMap { product =>
      if (product.isAvailable && product.quantity >= quantity) {
        val newQuantity = product.quantity - quantity
        val updated = product.copy(
          quantity = newQuantity,
          status = if (newQuantity == 0) OutOfStock else product.status
        )
        products = products + (id -> updated)
        Some(updated)
      } else {
        None
      }
    }
  }
  
  def getTotalInventoryValue: BigDecimal = {
    products.values.map(_.totalValue).sum
  }
  
  def getProductsByPriceRange(minPrice: BigDecimal, maxPrice: BigDecimal): List[Product] = {
    products.values.filter(p => p.price >= minPrice && p.price <= maxPrice).toList
  }
  
  /**
   * Ищет продукты по названию (без учета регистра)
   * 
   * @param namePattern часть названия для поиска
   * @return список найденных продуктов
   */
  def searchProductsByName(namePattern: String): List[Product] = {
    val pattern = namePattern.toLowerCase
    products.values.filter(p => p.name.toLowerCase.contains(pattern)).toList
  }
  
  /**
   * Получает продукты с низким запасом
   * 
   * @param threshold минимальный порог количества
   * @return список продуктов с количеством ниже порога
   */
  def getLowStockProducts(threshold: Int = 10): List[Product] = {
    products.values.filter(_.quantity < threshold).toList
  }
  
  /**
   * Получает топ N самых дорогих продуктов
   * 
   * @param n количество продуктов
   * @return список продуктов, отсортированных по цене (убывание)
   */
  def getTopExpensiveProducts(n: Int): List[Product] = {
    products.values.toList.sortBy(_.price)(Ordering[BigDecimal].reverse).take(n)
  }
}
