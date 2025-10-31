package com.example.models

/**
 * Модель пользователя с использованием case class для неизменяемости
 */
case class User(
  id: Long,
  name: String,
  email: String,
  age: Int,
  isActive: Boolean = true
) {
  def canVote: Boolean = age >= 18
  
  def displayName: String = s"$name ($email)"
}

object User {
  def create(id: Long, name: String, email: String, age: Int): Option[User] = {
    if (age > 0 && age < 150 && email.contains("@")) {
      Some(User(id, name, email, age))
    } else {
      None
    }
  }
}

