package com.example.models

/**
 * Модель пользователя системы.
 * 
 * @param id уникальный идентификатор пользователя
 * @param name полное имя пользователя
 * @param email электронная почта пользователя (валидируется при создании)
 * @param age возраст пользователя (должен быть от 1 до 149)
 * @param isActive активен ли пользователь в системе
 */
case class User(
  id: Long,
  name: String,
  email: String,
  age: Int,
  isActive: Boolean = true
) {
  /** Проверяет, может ли пользователь голосовать (возраст >= 18 лет) */
  def canVote: Boolean = age >= 18
  
  /** Возвращает отображаемое имя пользователя с email */
  def displayName: String = s"$name ($email)"
  
  /** Определяет возрастную группу пользователя */
  def ageGroup: String = age match {
    case a if a < 18 => "Minor"
    case a if a < 65 => "Adult"
    case _ => "Senior"
  }
  
  /** Проверяет валидность данных пользователя */
  def isValid: Boolean = {
    val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,}|\\[\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\])$".r
    age > 0 && age < 150 && emailRegex.matches(email) && name.trim.nonEmpty
  }
  
  def updateAge(newAge: Int): Option[User] = {
    if (newAge > 0 && newAge < 150) Some(this.copy(age = newAge)) else None
  }
  
  def deactivate: User = this.copy(isActive = false)
  
  def activate: User = this.copy(isActive = true)
}

/**
 * Компаньон-объект для User с фабричными методами
 */
object User {
  /**
   * Создает нового пользователя с валидацией данных
   * 
   * @param id идентификатор пользователя
   * @param name имя пользователя
   * @param email email пользователя
   * @param age возраст пользователя
   * @return Some(User) если данные валидны, None иначе
   */
  def create(id: Long, name: String, email: String, age: Int): Option[User] = {
    val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,}|\\[\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\])$".r
    if (age > 0 && age < 150 && emailRegex.matches(email) && name.trim.nonEmpty && email.length <= 254) {
      Some(User(id, name.trim, email.toLowerCase, age))
    } else {
      None
    }
  }
  
  /**
   * Создает пользователя из строки формата "id,name,email,age"
   * 
   * @param data строка с данными пользователя через запятую
   * @return Some(User) при успешном парсинге, None иначе
   */
  def fromString(data: String): Option[User] = {
    val parts = data.split(",")
    if (parts.length == 4) {
      try {
        val id = parts(0).trim.toLong
        val name = parts(1).trim
        val email = parts(2).trim
        val age = parts(3).trim.toInt
        create(id, name, email, age)
      } catch {
        case _: NumberFormatException => None
      }
    } else {
      None
    }
  }
  
  val DefaultUser: User = User(0, "Unknown", "unknown@example.com", 0, isActive = false)
}

