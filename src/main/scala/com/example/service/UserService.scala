package com.example.service

import com.example.models.User
import scala.util.Try

/**
 * Сервис для управления пользователями
 * 
 * Предоставляет CRUD операции и статистику по пользователям
 */
class UserService {
  private var users: Map[Long, User] = Map.empty
  
  def addUser(user: User): Try[User] = Try {
    if (users.contains(user.id)) {
      throw new IllegalArgumentException(s"User with id ${user.id} already exists")
    }
    users = users + (user.id -> user)
    user
  }
  
  def getUser(id: Long): Option[User] = users.get(id)
  
  def getAllUsers: List[User] = users.values.toList
  
  def getActiveUsers: List[User] = users.values.filter(_.isActive).toList
  
  def getUsersByAge(minAge: Int, maxAge: Int): List[User] = {
    users.values.filter(u => u.age >= minAge && u.age <= maxAge).toList
  }
  
  /**
   * Ищет пользователей по имени или email (без учета регистра)
   * 
   * @param searchTerm поисковый запрос
   * @return список найденных пользователей
   */
  def searchUsers(searchTerm: String): List[User] = {
    val term = searchTerm.toLowerCase
    users.values.filter(u => 
      u.name.toLowerCase.contains(term) || u.email.toLowerCase.contains(term)
    ).toList
  }
  
  /**
   * Получает пользователей, отсортированных по возрасту
   * 
   * @param ascending true для сортировки по возрастанию, false - по убыванию
   * @return отсортированный список пользователей
   */
  def getUsersSortedByAge(ascending: Boolean = true): List[User] = {
    val sorted = users.values.toList.sortBy(_.age)
    if (ascending) sorted else sorted.reverse
  }
  
  /**
   * Проверяет, существует ли пользователь с заданным email
   * 
   * @param email email для проверки
   * @return true если пользователь существует, false иначе
   */
  def emailExists(email: String): Boolean = {
    users.values.exists(_.email.toLowerCase == email.toLowerCase)
  }
  
  def updateUser(id: Long, name: Option[String] = None, email: Option[String] = None): Option[User] = {
    users.get(id).map { user =>
      val updated = user.copy(
        name = name.getOrElse(user.name),
        email = email.getOrElse(user.email)
      )
      users = users + (id -> updated)
      updated
    }
  }
  
  def deleteUser(id: Long): Boolean = {
    if (users.contains(id)) {
      users = users - id
      true
    } else {
      false
    }
  }
  
  /**
   * Получает статистику по пользователям
   * 
   * @return объект со статистикой (количество, средний возраст и т.д.)
   */
  def getStatistics: UserStatistics = {
    val allUsers = users.values.toList
    if (allUsers.isEmpty) {
      UserStatistics(0, 0, 0.0, 0, 0, 0)
    } else {
      val activeCount = allUsers.count(_.isActive)
      val avgAge = allUsers.map(_.age).sum.toDouble / allUsers.size
      val canVoteCount = allUsers.count(_.canVote)
      val minAge = allUsers.map(_.age).min
      val maxAge = allUsers.map(_.age).max
      
      UserStatistics(allUsers.size, activeCount, avgAge, canVoteCount, minAge, maxAge)
    }
  }
}

/**
 * Статистика по пользователям
 */
case class UserStatistics(
  totalUsers: Int,
  activeUsers: Int,
  averageAge: Double,
  canVoteCount: Int,
  minAge: Int,
  maxAge: Int
)
