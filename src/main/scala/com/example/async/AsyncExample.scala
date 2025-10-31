package com.example.async

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Success, Failure}
import java.util.concurrent.Executors

/**
 * Демонстрация асинхронного программирования с Future
 */
object AsyncExample {
  
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))
  
  // Простые асинхронные операции
  def asyncCalculation(x: Int): Future[Int] = Future {
    Thread.sleep(100) // Имитация долгой операции
    x * 2
  }
  
  def asyncStringProcessing(s: String): Future[String] = Future {
    Thread.sleep(50)
    s.toUpperCase
  }
  
  // Композиция Future с map и flatMap
  def processData(x: Int): Future[String] = {
    asyncCalculation(x).map(result => s"Result: $result")
  }
  
  // For-comprehension с Future
  def combineAsyncOperations(x: Int, y: Int): Future[Int] = {
    for {
      result1 <- asyncCalculation(x)
      result2 <- asyncCalculation(y)
    } yield result1 + result2
  }
  
  // Обработка ошибок
  def safeAsyncOperation(x: Int): Future[Int] = Future {
    if (x < 0) throw new IllegalArgumentException("Negative number")
    x * 2
  }.recover {
    case _: IllegalArgumentException => 0
    case _: Exception => -1
  }
  
  // Обработка нескольких Future
  def combineMultipleFutures(numbers: List[Int]): Future[List[Int]] = {
    val futures = numbers.map(asyncCalculation)
    Future.sequence(futures)
  }
  
  // Первый успешный результат
  def firstSuccess[A](futures: List[Future[A]]): Future[Option[A]] = {
    Future.sequence(futures).map(_.headOption)
  }
  
  // Таймаут для Future (упрощенная версия)
  def withTimeout[A](future: Future[A], timeoutMs: Long): Future[Option[A]] = {
    Future {
      Thread.sleep(timeoutMs)
      None
    }.flatMap { _ =>
      Future.successful(None)
    }
  }
  
  // Цепочка операций с обработкой ошибок
  def chainOperations(x: Int): Future[String] = {
    asyncCalculation(x)
      .map(_ + 10)
      .map(_.toString)
      .recover {
        case e: Exception => s"Error: ${e.getMessage}"
      }
  }
  
  // Пример использования
  def example(): Unit = {
    val future1 = asyncCalculation(5)
    future1.onComplete {
      case Success(value) => println(s"Calculation result: $value")
      case Failure(exception) => println(s"Error: ${exception.getMessage}")
    }
    
    val future2 = combineAsyncOperations(3, 4)
    future2.foreach(result => println(s"Combined result: $result"))
    
    val future3 = combineMultipleFutures(List(1, 2, 3, 4, 5))
    future3.foreach(results => println(s"All results: $results"))
  }
}

