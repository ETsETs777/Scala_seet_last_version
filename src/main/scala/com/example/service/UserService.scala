package com.example.service

import com.example.models.User
import com.example.util.{Logger, CompositeLogger}
import scala.util.Try

/**
 * Сервис для управления пользователями
 * 
 * Предоставляет CRUD операции и статистику по пользователям
 */
class UserService(logger: Logger = CompositeLogger) {
  private var users: Map[Long, User] = Map.empty
  
  def addUser(user: User): Try[User] = Try {
    logger.debug(s"Attempting to add user: ${user.id}")
    if (users.contains(user.id)) {
      logger.warn(s"User with id ${user.id} already exists")
      throw new IllegalArgumentException(s"User with id ${user.id} already exists")
    }
    if (!user.isValid) {
      logger.error(s"Invalid user data: ${user.id}")
      throw new IllegalArgumentException(s"Invalid user data for id ${user.id}")
    }
    users = users + (user.id -> user)
    logger.info(s"User added successfully: ${user.id} - ${user.name}")
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
  
  /**
   * Экспортирует всех пользователей в формат CSV
   * 
   * @return строка в формате CSV
   */
  def exportToCSV: String = {
    val header = "id,name,email,age,isActive\n"
    val rows = users.values.map { u =>
      s"${u.id},${u.name},${u.email},${u.age},${u.isActive}"
    }.mkString("\n")
    logger.debug("Users exported to CSV format")
    header + rows
  }
  
  /**
   * Импортирует пользователей из CSV строки
   * 
   * @param csvData данные в формате CSV
   * @return количество успешно импортированных пользователей
   */
  def importFromCSV(csvData: String): Int = {
    logger.debug("Starting CSV import for users")
    val lines = csvData.split("\n").drop(1) // Пропускаем заголовок
    var imported = 0
    
    lines.foreach { line =>
      val parts = line.split(",")
      if (parts.length >= 4) {
        try {
          val id = parts(0).trim.toLong
          val name = parts(1).trim
          val email = parts(2).trim
          val age = parts(3).trim.toInt
          val isActive = if (parts.length > 4) parts(4).trim.toBoolean else true
          
          User.create(id, name, email, age).foreach { user =>
            val userWithActive = if (!isActive) user.deactivate else user
            addUser(userWithActive).foreach { u =>
              imported += 1
              logger.debug(s"Imported user: ${u.name}")
            }
          }
        } catch {
          case e: Exception =>
            logger.error(s"Failed to import user from line: $line", Some(e))
        }
      }
    }
    
    logger.info(s"CSV import completed: $imported users imported")
    imported
  }
  
  /**
   * Получает пользователей по возрастной группе
   * 
   * @param ageGroup группа: "Minor", "Adult", "Senior"
   * @return список пользователей в указанной группе
   */
  def getUsersByAgeGroup(ageGroup: String): List[User] = {
    users.values.filter(_.ageGroup == ageGroup).toList
  }
  
  /**
   * Деактивирует неактивных пользователей старше определенного возраста
   * 
   * @param maxAge максимальный возраст
   * @return количество деактивированных пользователей
   */
  def deactivateUsersOlderThan(maxAge: Int): Int = {
    logger.debug(s"Deactivating users older than $maxAge")
    var deactivated = 0
    users.foreach { case (id, user) =>
      if (user.age > maxAge && user.isActive) {
        users = users + (id -> user.deactivate)
        deactivated += 1
        logger.info(s"Deactivated user: ${user.id} - ${user.name} (age: ${user.age})")
      }
    }
    deactivated
  }
  
  def updateUser(id: Long, name: Option[String] = None, email: Option[String] = None): Option[User] = {
    logger.debug(s"Attempting to update user: $id")
    users.get(id).map { user =>
      val updated = user.copy(
        name = name.getOrElse(user.name),
        email = email.getOrElse(user.email)
      )
      if (!updated.isValid) {
        logger.error(s"Invalid user data after update: $id")
        throw new IllegalArgumentException(s"Invalid user data for id $id")
      }
      users = users + (id -> updated)
      logger.info(s"User updated successfully: $id")
      updated
    } match {
      case Some(u) => Some(u)
      case None =>
        logger.warn(s"User not found for update: $id")
        None
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
