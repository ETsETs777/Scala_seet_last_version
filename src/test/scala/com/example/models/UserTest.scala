package com.example.models

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class UserTest extends AnyFlatSpec with Matchers {
  
  "User" should "create valid user" in {
    val user = User.create(1, "Test User", "test@example.com", 25)
    user should be(Some(User(1, "Test User", "test@example.com", 25, true)))
  }
  
  it should "reject invalid age" in {
    User.create(1, "Test", "test@example.com", -5) should be(None)
    User.create(1, "Test", "test@example.com", 200) should be(None)
  }
  
  it should "reject invalid email" in {
    User.create(1, "Test", "invalid-email", 25) should be(None)
  }
  
  it should "check voting eligibility correctly" in {
    val adult = User(1, "Adult", "adult@example.com", 18)
    val minor = User(2, "Minor", "minor@example.com", 17)
    
    adult.canVote should be(true)
    minor.canVote should be(false)
  }
  
  it should "display name correctly" in {
    val user = User(1, "John Doe", "john@example.com", 30)
    user.displayName should be("John Doe (john@example.com)")
  }
}

