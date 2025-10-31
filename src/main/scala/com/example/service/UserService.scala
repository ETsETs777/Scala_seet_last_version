package com.example.service

import com.example.models.User
import scala.util.Try

/**
 * Сервис для работы с пользователями
 * Демонстрирует работу с Option, Try, и коллекциями
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
  
  def getStatistics: UserStatistics = {
    val allUsers = users.values.toList
    if (allUsers.isEmpty) {
      UserStatistics(0, 0, 0, 0, 0)
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

case class UserStatistics(
  totalUsers: Int,
  activeUsers: Int,
  averageAge: Double,
  canVoteCount: Int,
  minAge: Int,
  maxAge: Int
)

