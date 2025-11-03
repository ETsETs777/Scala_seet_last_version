package com.example.util

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration._

case class RateLimitConfig(maxRequests: Int, window: Duration)

class RateLimiter {
  private val requestCounts = new ConcurrentHashMap[String, (Long, Int)]()
  
  def isAllowed(key: String, config: RateLimitConfig): Boolean = {
    val now = System.currentTimeMillis()
    val windowStart = now - config.window.toMillis
    
    val (lastWindowStart, count) = requestCounts.getOrDefault(key, (now, 0))
    
    if (lastWindowStart < windowStart) {
      requestCounts.put(key, (now, 1))
      true
    } else {
      if (count < config.maxRequests) {
        requestCounts.put(key, (lastWindowStart, count + 1))
        true
      } else {
        false
      }
    }
  }
  
  def reset(key: String): Unit = {
    requestCounts.remove(key)
  }
  
  def resetAll(): Unit = {
    requestCounts.clear()
  }
  
  def getRemainingRequests(key: String, config: RateLimitConfig): Int = {
    val now = System.currentTimeMillis()
    val windowStart = now - config.window.toMillis
    val (lastWindowStart, count) = requestCounts.getOrDefault(key, (now, 0))
    
    if (lastWindowStart < windowStart) {
      config.maxRequests
    } else {
      math.max(0, config.maxRequests - count)
    }
  }
}


