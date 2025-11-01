package com.example.service

import com.example.models.{Product, ProductStatus, Active, OutOfStock}
import com.example.util.{Logger, CompositeLogger}
import scala.util.Try

/**
 * Сервис для управления продуктами
 * 
 * Предоставляет операции по управлению складом и продажами
 */
class ProductService(logger: Logger = CompositeLogger) {
  private var products: Map[Long, Product] = Map.empty
  private var nextId: Long = 1
  
  def addProduct(product: Product): Try[Product] = Try {
    logger.debug(s"Attempting to add product: ${product.name}")
    if (product.price <= 0) {
      logger.error(s"Invalid product price: ${product.price}")
      throw new IllegalArgumentException(s"Product price must be positive: ${product.price}")
    }
    if (product.quantity < 0) {
      logger.error(s"Invalid product quantity: ${product.quantity}")
      throw new IllegalArgumentException(s"Product quantity cannot be negative: ${product.quantity}")
    }
    val productWithId = product.copy(id = nextId)
    products = products + (nextId -> productWithId)
    logger.info(s"Product added successfully: $nextId - ${product.name}")
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
  
  /**
   * Применяет скидку к продукту
   * 
   * @param id идентификатор продукта
   * @param percentage процент скидки (0-100)
   * @return обновленный продукт или None если не найден
   */
  def applyDiscount(id: Long, percentage: BigDecimal): Option[Product] = {
    logger.debug(s"Applying discount $percentage% to product: $id")
    products.get(id).map { product =>
      val updated = product.applyDiscount(percentage)
      products = products + (id -> updated)
      logger.info(s"Discount applied to product $id: ${product.price} -> ${updated.price}")
      updated
    } match {
      case Some(p) => Some(p)
      case None => 
        logger.warn(s"Product not found for discount: $id")
        None
    }
  }
  
  /**
   * Применяет скидку ко всем продуктам с определенным статусом
   * 
   * @param status статус продуктов для применения скидки
   * @param percentage процент скидки (0-100)
   * @return список обновленных продуктов
   */
  def applyDiscountByStatus(status: ProductStatus, percentage: BigDecimal): List[Product] = {
    logger.info(s"Applying discount $percentage% to all products with status: $status")
    val updatedProducts = products.values
      .filter(_.status == status)
      .map { product =>
        val updated = product.applyDiscount(percentage)
        products = products + (product.id -> updated)
        updated
      }
      .toList
    logger.info(s"Discount applied to ${updatedProducts.size} products")
    updatedProducts
  }
  
  /**
   * Экспортирует все продукты в формат CSV
   * 
   * @return строка в формате CSV
   */
  def exportToCSV: String = {
    val header = "id,name,price,quantity,status,createdAt\n"
    val rows = products.values.map { p =>
      s"${p.id},${p.name},${p.price},${p.quantity},${p.status},${p.createdAt}"
    }.mkString("\n")
    logger.debug("Products exported to CSV format")
    header + rows
  }
  
  /**
   * Импортирует продукты из CSV строки
   * 
   * @param csvData данные в формате CSV
   * @return количество успешно импортированных продуктов
   */
  def importFromCSV(csvData: String): Int = {
    logger.debug("Starting CSV import")
    val lines = csvData.split("\n").drop(1) // Пропускаем заголовок
    var imported = 0
    
    lines.foreach { line =>
      val parts = line.split(",")
      if (parts.length >= 4) {
        try {
          val name = parts(1)
          val price = BigDecimal(parts(2))
          val quantity = parts(3).toInt
          
          Product.create(name, price, quantity).foreach { product =>
            addProduct(product).foreach { p =>
              imported += 1
              logger.debug(s"Imported product: ${p.name}")
            }
          }
        } catch {
          case e: Exception =>
            logger.error(s"Failed to import product from line: $line", Some(e))
        }
      }
    }
    
    logger.info(s"CSV import completed: $imported products imported")
    imported
  }
}
