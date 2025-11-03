package com.example.service

import org.scalatest.funsuite.AnyFunSuite
import com.example.models.User

class UserServiceRateLimitTest extends AnyFunSuite {
  test("addUser enforces validation errors") {
    val service = new UserService()
    val invalid = User(1, "", "bad", -5)
    val ex = intercept[IllegalArgumentException] {
      service.addUser(invalid).get
    }
    assert(ex.getMessage.nonEmpty)
  }

  test("addUser enforces rate limit") {
    val service = new UserService()
    val u1 = User(1, "John Doe", "john@example.com", 30)
    val u2 = User(2, "Jane Doe", "jane@example.com", 31)
    // simulate near-threshold by performing many calls
    // keep it small to avoid test slowness
    var thrown = false
    try {
      (1 to 110).foreach { i =>
        val u = User(i, s"U$i", s"u$i@example.com", 20)
        service.addUser(u)
      }
    } catch {
      case _: IllegalStateException => thrown = true
    }
    assert(thrown)
  }
}
