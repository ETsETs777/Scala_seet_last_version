package com.example.validation

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ValidationTest extends AnyFlatSpec with Matchers {
  
  "Validator" should "validate correct email" in {
    Validator.validateEmail("test@example.com") should be(Right("test@example.com"))
  }
  
  it should "reject invalid email" in {
    Validator.validateEmail("invalid-email") shouldBe a[Left[_, _]]
  }
  
  it should "validate correct age" in {
    Validator.validateAge(25) should be(Right(25))
    Validator.validateAge(1) should be(Right(1))
    Validator.validateAge(149) should be(Right(149))
  }
  
  it should "reject invalid age" in {
    Validator.validateAge(0) shouldBe a[Left[_, _]]
    Validator.validateAge(150) shouldBe a[Left[_, _]]
    Validator.validateAge(-10) shouldBe a[Left[_, _]]
  }
  
  it should "validate non-empty name" in {
    Validator.validateName("John Doe") should be(Right("John Doe"))
  }
  
  it should "reject empty name" in {
    Validator.validateName("") shouldBe a[Left[_, _]]
    Validator.validateName("   ") shouldBe a[Left[_, _]]
  }
  
  it should "combine validations correctly" in {
    val emailValid = Validator.validateEmail("test@example.com")
    val ageValid = Validator.validateAge(25)
    
    val combined = Validator.combineValidations(emailValid, ageValid)((e, a) => (e, a))
    combined should be(Right(("test@example.com", 25)))
  }
  
  it should "accumulate errors from multiple validations" in {
    val emailInvalid = Validator.validateEmail("invalid")
    val ageInvalid = Validator.validateAge(200)
    
    val combined = Validator.combineValidations(emailInvalid, ageInvalid)((e, a) => (e, a))
    combined shouldBe a[Left[_, _]]
    combined.left.get.length should be >= 2
  }
  
  "ErrorHandler" should "format errors correctly" in {
    val errors = List(InvalidEmail("bad@email"), InvalidAge(200))
    ErrorHandler.formatErrors(errors) should include("Invalid email")
    ErrorHandler.formatErrors(errors) should include("Invalid age")
  }
  
  it should "check if all validations are valid" in {
    val results = List(
      Validator.validateEmail("test@example.com"),
      Validator.validateAge(25),
      Validator.validateName("John")
    )
    ErrorHandler.isAllValid(results) should be(true)
  }
}

