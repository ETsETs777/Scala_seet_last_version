package com.example.functional

import scala.util.Try

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
  
  def divideWithTry(a: Double, b: Double): Try[Double] = Try {
    if (b == 0) throw new ArithmeticException("Division by zero")
    a / b
  }
  
  def parseDoubleWithTry(s: String): Try[Double] = Try(s.toDouble)
  
  def safeDivideWithTry(a: String, b: String): Try[Double] = {
    for {
      numA <- parseDoubleWithTry(a)
      numB <- parseDoubleWithTry(b)
      result <- divideWithTry(numA, numB)
    } yield result
  }
  
  type ErrorMessage = String
  type Result[A] = Either[ErrorMessage, A]
  
  def divideEither(a: Double, b: Double): Result[Double] = {
    if (b == 0) Left("Division by zero")
    else Right(a / b)
  }
  
  def parseDoubleEither(s: String): Result[Double] = {
    try {
      Right(s.toDouble)
    } catch {
      case _: NumberFormatException => Left(s"Cannot parse '$s' as number")
    }
  }
  
  def safeDivideEither(a: String, b: String): Result[Double] = {
    for {
      numA <- parseDoubleEither(a)
      numB <- parseDoubleEither(b)
      result <- divideEither(numA, numB)
    } yield result
  }
}

