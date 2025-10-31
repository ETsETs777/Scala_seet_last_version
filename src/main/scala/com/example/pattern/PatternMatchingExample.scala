package com.example.pattern

import com.example.models.{Product, Active, Discontinued, OutOfStock}

object PatternMatchingExample {
  
  def describeProduct(product: Product): String = product.status match {
    case Active if product.quantity > 10 => s"${product.name} - В наличии (много)"
    case Active if product.quantity > 0 => s"${product.name} - В наличии (мало)"
    case OutOfStock => s"${product.name} - Нет в наличии"
    case Discontinued => s"${product.name} - Снят с производства"
    case _ => s"${product.name} - Неизвестный статус"
  }
  
  def processNumber(n: Any): String = n match {
    case i: Int if i > 0 => s"Положительное целое: $i"
    case i: Int if i < 0 => s"Отрицательное целое: $i"
    case 0 => "Ноль"
    case d: Double => s"Дробное число: $d"
    case s: String => s"Строка: $s"
    case _ => "Неизвестный тип"
  }
  
  def extractUserInfo(info: (String, Int, String)): String = info match {
    case (name, age, email) if age >= 18 => s"$name ($age лет) - $email - может голосовать"
    case (name, age, email) => s"$name ($age лет) - $email - не может голосовать"
  }
  
  def listProcessor(list: List[Int]): String = list match {
    case Nil => "Пустой список"
    case head :: Nil => s"Один элемент: $head"
    case head :: second :: Nil => s"Два элемента: $head и $second"
    case head :: tail => s"Первый: $head, остальных: ${tail.length}"
  }
}
