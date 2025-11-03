package com.example.util

trait ValidationRule[T] {
  def validate(value: T): Option[String]
}

object Validator {
  def validate[T](value: T, rules: ValidationRule[T]*): List[String] = {
    rules.flatMap(_.validate(value)).toList
  }
  
  def isValid[T](value: T, rules: ValidationRule[T]*): Boolean = {
    validate(value, rules: _*).isEmpty
  }
  
  def notEmpty: ValidationRule[String] = new ValidationRule[String] {
    def validate(value: String): Option[String] = {
      if (value == null || value.trim.isEmpty) Some("Value cannot be empty") else None
    }
  }
  
  def minLength(min: Int): ValidationRule[String] = new ValidationRule[String] {
    def validate(value: String): Option[String] = {
      if (value == null || value.length < min) Some(s"Value must be at least $min characters") else None
    }
  }
  
  def maxLength(max: Int): ValidationRule[String] = new ValidationRule[String] {
    def validate(value: String): Option[String] = {
      if (value != null && value.length > max) Some(s"Value must be at most $max characters") else None
    }
  }
  
  def email: ValidationRule[String] = new ValidationRule[String] {
    def validate(value: String): Option[String] = {
      val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".r
      if (value == null || !emailRegex.matches(value)) Some("Invalid email format") else None
    }
  }
  
  def range(min: Int, max: Int): ValidationRule[Int] = new ValidationRule[Int] {
    def validate(value: Int): Option[String] = {
      if (value < min || value > max) Some(s"Value must be between $min and $max") else None
    }
  }
  
  def positive: ValidationRule[Int] = new ValidationRule[Int] {
    def validate(value: Int): Option[String] = {
      if (value <= 0) Some("Value must be positive") else None
    }
  }
  
  def positiveBigDecimal: ValidationRule[BigDecimal] = new ValidationRule[BigDecimal] {
    def validate(value: BigDecimal): Option[String] = {
      if (value <= 0) Some("Value must be positive") else None
    }
  }
  
  def matches(pattern: String): ValidationRule[String] = new ValidationRule[String] {
    def validate(value: String): Option[String] = {
      if (value == null || !value.matches(pattern)) Some(s"Value does not match pattern: $pattern") else None
    }
  }
}


