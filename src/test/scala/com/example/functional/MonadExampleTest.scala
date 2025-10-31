package com.example.functional

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MonadExampleTest extends AnyFlatSpec with Matchers {
  
  "MonadExample" should "divide numbers correctly" in {
    MonadExample.divide(10, 2) should be(Some(5.0))
    MonadExample.divide(10, 0) should be(None)
  }
  
  it should "parse valid doubles" in {
    MonadExample.parseDouble("10.5") should be(Some(10.5))
    MonadExample.parseDouble("abc") should be(None)
  }
  
  it should "safely divide strings" in {
    MonadExample.safeDivide("10", "2") should be(Some(5.0))
    MonadExample.safeDivide("10", "0") should be(None)
    MonadExample.safeDivide("abc", "2") should be(None)
  }
  
  it should "process list of numbers" in {
    val numbers = List("1", "2", "-3", "4.5", "abc", "6")
    val result = MonadExample.processNumbers(numbers)
    result should contain allOf(1.0, 2.0, 4.5, 6.0)
    result should not contain -3.0
  }
  
  it should "find first positive number" in {
    val numbers = List("-1", "2", "3")
    MonadExample.findFirstPositive(numbers) should be(Some(2.0))
    
    val allNegative = List("-1", "-2", "-3")
    MonadExample.findFirstPositive(allNegative) should be(None)
  }
}

