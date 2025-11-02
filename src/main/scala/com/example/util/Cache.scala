package com.example.util

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration._

case class CacheEntry[T](value: T, expiresAt: Long)

class Cache[T](defaultTTL: Duration = 1.hour) {
  private val storage = new ConcurrentHashMap[String, CacheEntry[T]]
  
  def put(key: String, value: T, ttl: Duration = defaultTTL): Unit = {
    val expiresAt = System.currentTimeMillis() + ttl.toMillis
    storage.put(key, CacheEntry(value, expiresAt))
    cleanup()
  }
  
  def get(key: String): Option[T] = {
    Option(storage.get(key)).flatMap { entry =>
      if (System.currentTimeMillis() < entry.expiresAt) {
        Some(entry.value)
      } else {
        storage.remove(key)
        None
      }
    }
  }
  
  def remove(key: String): Option[T] = {
    Option(storage.remove(key)).map(_.value)
  }
  
  def contains(key: String): Boolean = {
    get(key).isDefined
  }
  
  def clear(): Unit = {
    storage.clear()
  }
  
  def size: Int = {
    cleanup()
    storage.size()
  }
  
  private def cleanup(): Unit = {
    val now = System.currentTimeMillis()
    val iterator = storage.entrySet().iterator()
    while (iterator.hasNext) {
      val entry = iterator.next()
      if (now >= entry.getValue.expiresAt) {
        iterator.remove()
      }
    }
  }
  
  def getOrElseUpdate(key: String, defaultValue: => T, ttl: Duration = defaultTTL): T = {
    get(key).getOrElse {
      val value = defaultValue
      put(key, value, ttl)
      value
    }
  }
}



