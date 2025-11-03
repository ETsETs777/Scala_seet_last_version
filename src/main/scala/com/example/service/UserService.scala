package com.example.service

import com.example.models.User
import com.example.util.{Logger, CompositeLogger, Pagination, RateLimiter, RateLimitConfig, Validator}
import com.example.event.{GlobalEventBus, UserCreatedEvent, UserUpdatedEvent, UserDeletedEvent}
import com.example.metrics.GlobalMetrics
import scala.util.Try


class UserService(logger: Logger = CompositeLogger) {
  private var users: Map[Long, User] = Map.empty
  private val searchIndex = new com.example.util.SearchIndex[User]()
  private val addLimiter = new RateLimiter()
  
  def addUser(user: User): Try[User] = Try {
    if (!addLimiter.isAllowed("user:add", RateLimitConfig(maxRequests = 100, window = scala.concurrent.duration.Duration(60, scala.concurrent.duration.SECONDS)))) {
      throw new IllegalStateException("Rate limit exceeded for addUser")
    }
    val validationErrors = Validator.validate(user.email, Validator.email) ++
      Validator.validate(user.name, Validator.notEmpty, Validator.minLength(2)) ++
      Validator.validate(user.age, Validator.range(1, 150))
    if (validationErrors.nonEmpty) {
      throw new IllegalArgumentException(validationErrors.mkString("; "))
    }
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
    searchIndex.indexItems(List(user.name, user.email), user)
    logger.info(s"User added successfully: ${user.id} - ${user.name}")
    GlobalEventBus.publish(UserCreatedEvent(user.id, user.name))
    GlobalMetrics.incrementCounter("users.created")
    user
  }
  
  def getUser(id: Long): Option[User] = users.get(id)
  
  def getAllUsers: List[User] = users.values.toList
  
  def getActiveUsers: List[User] = users.values.filter(_.isActive).toList
  
  def getUsersByAge(minAge: Int, maxAge: Int): List[User] = {
    users.values.filter(u => u.age >= minAge && u.age <= maxAge).toList
  }
  
  
  def searchUsers(searchTerm: String): List[User] = {
    searchIndex.search(searchTerm).toList
  }
  
  
  def getUsersSortedByAge(ascending: Boolean = true): List[User] = {
    val sorted = users.values.toList.sortBy(_.age)
    if (ascending) sorted else sorted.reverse
  }
  
  
  def emailExists(email: String): Boolean = {
    users.values.exists(_.email.toLowerCase == email.toLowerCase)
  }
  
  
  def exportToCSV: String = {
    val header = "id,name,email,age,isActive\n"
    val rows = users.values.map { u =>
      s"${u.id},${u.name},${u.email},${u.age},${u.isActive}"
    }.mkString("\n")
    logger.debug("Users exported to CSV format")
    header + rows
  }
  
  
  def importFromCSV(csvData: String): Int = {
    logger.debug("Starting CSV import for users")
    val lines = csvData.split("\n").drop(1) 
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
  
  
  def getUsersByAgeGroup(ageGroup: String): List[User] = {
    users.values.filter(_.ageGroup == ageGroup).toList
  }
  
  
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
      GlobalEventBus.publish(UserUpdatedEvent(id))
      GlobalMetrics.incrementCounter("users.updated")
      updated
    } match {
      case Some(u) => Some(u)
      case None =>
        logger.warn(s"User not found for update: $id")
        None
    }
  }
  
  
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
  
  
  def getUsersPaginated(page: Int, pageSize: Int): Pagination.PageResult[User] = {
    GlobalMetrics.time("users.paginated") {
      Pagination.validate(page, pageSize).map { case (validPage, validSize) =>
        Pagination.paginate(getAllUsers, validPage, validSize)
      }.getOrElse(Pagination.emptyPage[User](page, pageSize))
    }
  }
  
  def bulkAddUsers(usersList: List[User]): List[Try[User]] = {
    usersList.map(addUser)
  }
  
  def getUserCount: Int = users.size
  
  def getUserAgeDistribution: Map[String, Int] = {
    users.values.groupBy(_.ageGroup).mapValues(_.size)
  }
  
  def getUsersWithEmailDomain(domain: String): List[User] = {
    users.values.filter(_.email.endsWith(s"@$domain")).toList
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
