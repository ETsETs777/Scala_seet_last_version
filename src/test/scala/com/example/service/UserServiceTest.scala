package com.example.service

import com.example.models.User
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class UserServiceTest extends AnyFlatSpec with Matchers {
  
  "UserService" should "add user successfully" in {
    val service = new UserService()
    val user = User(1, "Test", "test@example.com", 25)
    
    service.addUser(user).isSuccess should be(true)
    service.getUser(1) should be(Some(user))
  }
  
  it should "not allow duplicate user ids" in {
    val service = new UserService()
    val user1 = User(1, "Test1", "test1@example.com", 25)
    val user2 = User(1, "Test2", "test2@example.com", 30)
    
    service.addUser(user1).isSuccess should be(true)
    service.addUser(user2).isFailure should be(true)
  }
  
  it should "return None for non-existent user" in {
    val service = new UserService()
    service.getUser(999) should be(None)
  }
  
  it should "filter active users" in {
    val service = new UserService()
    service.addUser(User(1, "Active", "active@example.com", 25, true))
    service.addUser(User(2, "Inactive", "inactive@example.com", 30, false))
    
    service.getActiveUsers.length should be(1)
    service.getActiveUsers.head.name should be("Active")
  }
  
  it should "filter users by age range" in {
    val service = new UserService()
    service.addUser(User(1, "Young", "young@example.com", 20))
    service.addUser(User(2, "Middle", "middle@example.com", 30))
    service.addUser(User(3, "Old", "old@example.com", 50))
    
    val filtered = service.getUsersByAge(25, 40)
    filtered.length should be(1)
    filtered.head.name should be("Middle")
  }
  
  it should "update user successfully" in {
    val service = new UserService()
    service.addUser(User(1, "Old Name", "old@example.com", 25))
    
    service.updateUser(1, Some("New Name"), None)
    service.getUser(1).get.name should be("New Name")
  }
  
  it should "calculate statistics correctly" in {
    val service = new UserService()
    service.addUser(User(1, "User1", "user1@example.com", 20))
    service.addUser(User(2, "User2", "user2@example.com", 30))
    service.addUser(User(3, "User3", "user3@example.com", 40))
    
    val stats = service.getStatistics
    stats.totalUsers should be(3)
    stats.activeUsers should be(3)
    stats.averageAge should be(30.0)
    stats.canVoteCount should be(3)
  }
}

