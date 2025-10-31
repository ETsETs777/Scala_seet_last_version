package com.example.functional

/**
 * Демонстрация работы с Option и Try (монадные операции)
 */
object MonadExample {
  
  def divide(a: Double, b: Double): Option[Double] = {
    if (b != 0) Some(a / b) else None
  }
  
  def parseDouble(s: String): Option[Double] = {
    try {
      Some(s.toDouble)
    } catch {
      case _: NumberFormatException => None
    }
  }
  
  def safeDivide(a: String, b: String): Option[Double] = {
    for {
      numA <- parseDouble(a)
      numB <- parseDouble(b)
      result <- divide(numA, numB)
    } yield result
  }
  
  def processNumbers(numbers: List[String]): List[Double] = {
    numbers.flatMap(parseDouble).filter(_ > 0)
  }
  
  def findFirstPositive(numbers: List[String]): Option[Double] = {
    numbers.flatMap(parseDouble).find(_ > 0)
  }
}

