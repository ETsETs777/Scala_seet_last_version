package com.example

import com.example.models.{User, Product}
import com.example.service.{UserService, ProductService}
import com.example.util.Extensions._
import com.example.functional.MonadExample
import com.example.pattern.PatternMatchingExample
import com.example.serialization.JsonSerializer
import com.example.event.{GlobalEventBus, EventHandler}
import com.example.metrics.GlobalMetrics


object Main {
  def main(args: Array[String]): Unit = {
    println("=" * 50)
    println("Демонстрация возможностей Scala")
    println("=" * 50)
    
    setupEventHandlers()
    
    demonstrateUserService()
    
    demonstrateProductService()
    
    demonstratePatternMatching()
    
    demonstrateFunctionalProgramming()
    
    demonstrateExtensions()
    
    demonstrateMetrics()
    
    println("\n" + "=" * 50)
    println("Демонстрация завершена!")
    println("=" * 50)
  }
  
  def demonstrateUserService(): Unit = {
    println("\n--- Работа с пользователями ---")
    val userService = new UserService()
    
    User.create(1, "Иван Иванов", "ivan@example.com", 25).foreach(userService.addUser)
    User.create(2, "Мария Петрова", "maria@example.com", 30).foreach(userService.addUser)
    User.create(3, "Петр Сидоров", "petr@example.com", 17).foreach(userService.addUser)
    User.create(4, "Анна Козлова", "anna@example.com", 72).foreach(userService.addUser)
    
    println("Все пользователи:")
    userService.getAllUsers.foreach(u => println(s"  - ${u.displayName}, возраст: ${u.age}, может голосовать: ${u.canVote}"))
    
    println("\nСтатистика:")
    val stats = userService.getStatistics
    println(s"  Всего: ${stats.totalUsers}")
    println(s"  Активных: ${stats.activeUsers}")
    println(f"  Средний возраст: ${stats.averageAge}%.2f")
    println(s"  Могут голосовать: ${stats.canVoteCount}")
    println(s"  Возраст от ${stats.minAge} до ${stats.maxAge} лет")
    
    println("\nПоиск пользователей по запросу 'иван':")
    userService.searchUsers("иван").foreach(u => println(s"  - ${u.displayName}"))
    
    println("\nПользователи, отсортированные по возрасту (возрастание):")
    userService.getUsersSortedByAge(ascending = true).foreach(u => println(s"  - ${u.name}: ${u.age} лет"))
    
    println("\nПользователи группы Adult:")
    userService.getUsersByAgeGroup("Adult").foreach(u => println(s"  - ${u.name}: ${u.age} лет"))
    
    println("\nЭкспорт пользователей в CSV:")
    val csvData = userService.exportToCSV
    println(csvData.split("\n").take(3).mkString("\n") + "...")
    
    println("\nПагинация пользователей (страница 1, размер 2):")
    val page = userService.getUsersPaginated(1, 2)
    println(s"  Страница ${page.page} из ${page.totalPages}, всего элементов: ${page.totalItems}")
    page.items.foreach(u => println(s"  - ${u.name}"))
    
    println("\nJSON сериализация пользователя:")
    userService.getAllUsers.headOption.foreach { user =>
      val json = JsonSerializer.toJson(user)
      println(json)
    }
  }
  
  def demonstrateProductService(): Unit = {
    println("\n--- Работа с продуктами ---")
    val productService = new ProductService()
    
    Product.create("Ноутбук", 50000, 10).foreach(productService.addProduct)
    Product.create("Мышь", 1500, 25).foreach(productService.addProduct)
    Product.create("Клавиатура", 3000, 0).foreach(productService.addProduct)
    
    println("Все продукты:")
    productService.getAllProducts.foreach { p =>
      println(s"  - ${p.name}: ${p.price} руб., количество: ${p.quantity}, доступен: ${p.isAvailable}")
    }
    
    println(s"\nОбщая стоимость инвентаря: ${productService.getTotalInventoryValue} руб.")
    println("\nДоступные продукты:")
    productService.getAvailableProducts.foreach(p => println(s"  - ${p.name}"))
    
    println("\nПоиск продуктов по названию 'мышь':")
    productService.searchProductsByName("мышь").foreach(p => println(s"  - ${p.name}: ${p.price} руб."))
    
    println("\nПродукты с низким запасом (< 15):")
    productService.getLowStockProducts(15).foreach(p => println(s"  - ${p.name}: ${p.quantity} шт."))
    
    println("\nТоп-2 самых дорогих продукта:")
    productService.getTopExpensiveProducts(2).foreach(p => println(s"  - ${p.name}: ${p.price} руб."))
    
    println("\nПрименение скидки 10% к первому продукту:")
    productService.getAllProducts.headOption.foreach { product =>
      productService.applyDiscount(product.id, BigDecimal(10)).foreach { updated =>
        println(s"  ${product.name}: ${product.price} руб. -> ${updated.price} руб.")
      }
    }
    
    println("\nЭкспорт продуктов в CSV:")
    val productCSV = productService.exportToCSV
    println(productCSV.split("\n").take(3).mkString("\n") + "...")
    
    println("\nПагинация продуктов (страница 1, размер 2):")
    val page = productService.getProductsPaginated(1, 2)
    println(s"  Страница ${page.page} из ${page.totalPages}, всего элементов: ${page.totalItems}")
    page.items.foreach(p => println(s"  - ${p.name}"))
    
    println("\nJSON сериализация продукта:")
    productService.getAllProducts.headOption.foreach { product =>
      val json = JsonSerializer.toJson(product)
      println(json)
    }
  }
  
  def demonstratePatternMatching(): Unit = {
    println("\n--- Pattern Matching ---")
    
    val numbers: List[Any] = List(1, -5, 0, 3.14, "test")
    numbers.foreach(n => println(s"  ${PatternMatchingExample.processNumber(n)}"))
    
    val userInfo = ("Алексей", 28, "alex@example.com")
    println(s"\n  ${PatternMatchingExample.extractUserInfo(userInfo)}")
    
    val lists = List(
      List(),
      List(1),
      List(1, 2),
      List(1, 2, 3, 4, 5)
    )
    lists.foreach(list => println(s"  ${PatternMatchingExample.listProcessor(list)}"))
  }
  
  def demonstrateFunctionalProgramming(): Unit = {
    println("\n--- Функциональное программирование ---")
    
    val result1 = MonadExample.safeDivide("10", "2")
    val result2 = MonadExample.safeDivide("10", "0")
    val result3 = MonadExample.safeDivide("abc", "2")
    
    println(s"  10 / 2 = ${result1.getOrElse("Ошибка")}")
    println(s"  10 / 0 = ${result2.getOrElse("Ошибка деления на ноль")}")
    println(s"  abc / 2 = ${result3.getOrElse("Ошибка парсинга")}")
    
    val numbers = List("1", "2", "-3", "4.5", "abc", "6")
    val positive = MonadExample.processNumbers(numbers)
    println(s"\n  Положительные числа из $numbers: $positive")
    
    val firstPositive = MonadExample.findFirstPositive(numbers)
    println(s"  Первое положительное: ${firstPositive.getOrElse("Не найдено")}")
  }
  
  def demonstrateExtensions(): Unit = {
    println("\n--- Расширения (Type Enrichment) ---")
    
    val text = "hello world scala"
    println(s"  '$text' -> '${text.capitalizeWords}'")
    
    val emails = List("valid@email.com", "invalid", "another@test.ru")
    emails.foreach(email => 
      println(s"  '$email' - валидный email: ${email.isValidEmail}")
    )
    
    val numbers = List(4, 5, 6)
    numbers.foreach(n => 
      println(s"  $n - четное: ${n.isEven}, факториал: ${n.factorial}")
    )
    
    val words = List("apple", "banana", "apple", "orange", "banana", "apple")
    println(s"\n  Подсчет вхождений: ${words.groupByCount}")
  }
  
  def setupEventHandlers(): Unit = {
    GlobalEventBus.subscribe("user.created") {
      new EventHandler[com.example.event.UserCreatedEvent] {
        def handle(event: com.example.event.UserCreatedEvent): Unit = {
          println(s"[Event] User created: ${event.userId} - ${event.userName}")
        }
      }
    }
    
    GlobalEventBus.subscribe("product.created") {
      new EventHandler[com.example.event.ProductCreatedEvent] {
        def handle(event: com.example.event.ProductCreatedEvent): Unit = {
          println(s"[Event] Product created: ${event.productId} - ${event.productName}")
        }
      }
    }
  }
  
  def demonstrateMetrics(): Unit = {
    println("\n--- Метрики ---")
    
    println("\nСводка метрик:")
    println(GlobalMetrics.getMetricsSummary)
    
    println("\nИстория событий:")
    GlobalEventBus.getEventHistory.take(5).foreach { event =>
      println(s"  [${event.timestamp}] ${event.eventType}")
    }
  }
}
