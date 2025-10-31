package com.example.service

import com.example.models.User
import scala.util.Try

/**
 * Сервис для работы с пользователями.
 * 
 * Предоставляет CRUD операции для управления пользователями:
 * - Добавление пользователей с проверкой дубликатов
 * - Получение пользователей по ID, статусу, возрасту
 * - Обновление информации о пользователях
 * - Удаление пользователей
 * - Получение статистики
 * 
 * Демонстрирует работу с Option, Try, и коллекциями в Scala.
 * 
 * @example {{{
 *   val service = new UserService()
 *   val user = User(1, "John", "john@example.com", 25)
 *   service.addUser(user)
 *   val allUsers = service.getAllUsers
 * }}}
 */
class UserService {
  /** Хранилище пользователей в виде Map[id -> User] */
  private var users: Map[Long, User] = Map.empty
  
  /**
   * Добавляет нового пользователя в систему.
   * 
   * @param user Пользователь для добавления
   * @return Try[User] - успешно добавленный пользователь или ошибка, если пользователь с таким ID уже существует
   */
  def addUser(user: User): Try[User] = Try {
    if (users.contains(user.id)) {
      throw new IllegalArgumentException(s"User with id ${user.id} already exists")
    }
    users = users + (user.id -> user)
    user
  }
  
  /**
   * Получает пользователя по ID.
   * 
   * @param id Идентификатор пользователя
   * @return Option[User] - пользователь, если найден, иначе None
   */
  def getUser(id: Long): Option[User] = users.get(id)
  
  /**
   * Возвращает всех пользователей.
   * 
   * @return List[User] - список всех пользователей
   */
  def getAllUsers: List[User] = users.values.toList
  
  /**
   * Возвращает только активных пользователей.
   * 
   * @return List[User] - список активных пользователей
   */
  def getActiveUsers: List[User] = users.values.filter(_.isActive).toList
  
  /**
   * Возвращает пользователей в заданном диапазоне возрастов.
   * 
   * @param minAge Минимальный возраст (включительно)
   * @param maxAge Максимальный возраст (включительно)
   * @return List[User] - список пользователей в заданном диапазоне
   */
  def getUsersByAge(minAge: Int, maxAge: Int): List[User] = {
    users.values.filter(u => u.age >= minAge && u.age <= maxAge).toList
  }
  
  /**
   * Обновляет информацию о пользователе.
   * 
   * @param id Идентификатор пользователя
   * @param name Новое имя (опционально)
   * @param email Новый email (опционально)
   * @return Option[User] - обновленный пользователь, если найден
   */
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
  
  /**
   * Удаляет пользователя из системы.
   * 
   * @param id Идентификатор пользователя
   * @return Boolean - true, если пользователь был удален, false если не найден
   */
  def deleteUser(id: Long): Boolean = {
    if (users.contains(id)) {
      users = users - id
      true
    } else {
      false
    }
  }
  
  /**
   * Вычисляет статистику по пользователям.
   * 
   * @return UserStatistics - объект со статистикой:
   *         - totalUsers: общее количество пользователей
   *         - activeUsers: количество активных пользователей
   *         - averageAge: средний возраст
   *         - canVoteCount: количество пользователей, которые могут голосовать
   *         - minAge: минимальный возраст
   *         - maxAge: максимальный возраст
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
 * Статистика по пользователям.
 * 
 * @param totalUsers Общее количество пользователей
 * @param activeUsers Количество активных пользователей
 * @param averageAge Средний возраст пользователей
 * @param canVoteCount Количество пользователей, которые могут голосовать (>= 18 лет)
 * @param minAge Минимальный возраст среди пользователей
 * @param maxAge Максимальный возраст среди пользователей
 */
case class UserStatistics(
  totalUsers: Int,
  activeUsers: Int,
  averageAge: Double,
  canVoteCount: Int,
  minAge: Int,
  maxAge: Int
)

