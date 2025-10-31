package com.example.validation

import scala.util.{Try, Success, Failure}

/**
 * Система валидации данных
 */
sealed trait ValidationError {
  def message: String
}

case class InvalidEmail(email: String) extends ValidationError {
  def message: String = s"Invalid email format: $email"
}

case class InvalidAge(age: Int) extends ValidationError {
  def message: String = s"Invalid age: $age (must be between 1 and 150)"
}

case class InvalidName(name: String) extends ValidationError {
  def message: String = s"Invalid name: '$name' (must not be empty)"
}

case class InvalidPrice(price: BigDecimal) extends ValidationError {
  def message: String = s"Invalid price: $price (must be positive)"
}

case class InvalidQuantity(quantity: Int) extends ValidationError {
  def message: String = s"Invalid quantity: $quantity (must be non-negative)"
}

type ValidationResult[A] = Either[List[ValidationError], A]

object Validator {
  
  def validateEmail(email: String): ValidationResult[String] = {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".r
    if (emailRegex.matches(email)) {
      Right(email)
    } else {
      Left(List(InvalidEmail(email)))
    }
  }
  
  def validateAge(age: Int): ValidationResult[Int] = {
    if (age > 0 && age < 150) {
      Right(age)
    } else {
      Left(List(InvalidAge(age)))
    }
  }
  
  def validateName(name: String): ValidationResult[String] = {
    if (name.trim.nonEmpty) {
      Right(name.trim)
    } else {
      Left(List(InvalidName(name)))
    }
  }
  
  def validatePrice(price: BigDecimal): ValidationResult[BigDecimal] = {
    if (price > 0) {
      Right(price)
    } else {
      Left(List(InvalidPrice(price)))
    }
  }
  
  def validateQuantity(quantity: Int): ValidationResult[Int] = {
    if (quantity >= 0) {
      Right(quantity)
    } else {
      Left(List(InvalidQuantity(quantity)))
    }
  }
  
  // Комбинированная валидация
  def combineValidations[A, B, C](
    va: ValidationResult[A],
    vb: ValidationResult[B]
  )(f: (A, B) => C): ValidationResult[C] = {
    (va, vb) match {
      case (Right(a), Right(b)) => Right(f(a, b))
      case (Left(errorsA), Left(errorsB)) => Left(errorsA ++ errorsB)
      case (Left(errors), _) => Left(errors)
      case (_, Left(errors)) => Left(errors)
    }
  }
  
  def combineValidations[A, B, C, D](
    va: ValidationResult[A],
    vb: ValidationResult[B],
    vc: ValidationResult[C]
  )(f: (A, B, C) => D): ValidationResult[D] = {
    (va, vb, vc) match {
      case (Right(a), Right(b), Right(c)) => Right(f(a, b, c))
      case _ =>
        val errors = List(va, vb, vc).collect { case Left(errs) => errs }.flatten
        Left(errors)
    }
  }
}

// Обработка ошибок
object ErrorHandler {
  
  def handleValidation[A](result: ValidationResult[A]): Try[A] = {
    result match {
      case Right(value) => Success(value)
      case Left(errors) => Failure(new ValidationException(errors))
    }
  }
  
  def collectErrors(results: List[ValidationResult[_]]): List[ValidationError] = {
    results.collect { case Left(errors) => errors }.flatten
  }
  
  def formatErrors(errors: List[ValidationError]): String = {
    errors.map(_.message).mkString("; ")
  }
  
  def isAllValid(results: List[ValidationResult[_]]): Boolean = {
    results.forall(_.isRight)
  }
}

class ValidationException(val errors: List[ValidationError])
  extends Exception(ErrorHandler.formatErrors(errors))

