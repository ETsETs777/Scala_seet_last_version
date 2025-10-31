package com.example.util

import java.time.{LocalDateTime, Duration, Period}
import java.time.format.DateTimeFormatter

object DateTimeUtils {
  
  val defaultFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  val dateOnlyFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val timeOnlyFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
  
  def formatDateTime(dt: LocalDateTime): String = {
    dt.format(defaultFormatter)
  }
  
  def formatDate(dt: LocalDateTime): String = {
    dt.format(dateOnlyFormatter)
  }
  
  def formatTime(dt: LocalDateTime): String = {
    dt.format(timeOnlyFormatter)
  }
  
  def parseDateTime(str: String): Option[LocalDateTime] = {
    try {
      Some(LocalDateTime.parse(str, defaultFormatter))
    } catch {
      case _: Exception => None
    }
  }
  
  def daysBetween(start: LocalDateTime, end: LocalDateTime): Long = {
    Duration.between(start, end).toDays
  }
  
  def hoursBetween(start: LocalDateTime, end: LocalDateTime): Long = {
    Duration.between(start, end).toHours
  }
  
  def minutesBetween(start: LocalDateTime, end: LocalDateTime): Long = {
    Duration.between(start, end).toMinutes
  }
  
  def isBefore(dt1: LocalDateTime, dt2: LocalDateTime): Boolean = {
    dt1.isBefore(dt2)
  }
  
  def isAfter(dt1: LocalDateTime, dt2: LocalDateTime): Boolean = {
    dt1.isAfter(dt2)
  }
  
  def addDays(dt: LocalDateTime, days: Long): LocalDateTime = {
    dt.plusDays(days)
  }
  
  def addHours(dt: LocalDateTime, hours: Long): LocalDateTime = {
    dt.plusHours(hours)
  }
  
  def addMinutes(dt: LocalDateTime, minutes: Long): LocalDateTime = {
    dt.plusMinutes(minutes)
  }
  
  def subtractDays(dt: LocalDateTime, days: Long): LocalDateTime = {
    dt.minusDays(days)
  }
  
  def isToday(dt: LocalDateTime): Boolean = {
    val today = LocalDateTime.now()
    dt.toLocalDate == today.toLocalDate
  }
  
  def isPast(dt: LocalDateTime): Boolean = {
    dt.isBefore(LocalDateTime.now())
  }
  
  def isFuture(dt: LocalDateTime): Boolean = {
    dt.isAfter(LocalDateTime.now())
  }
  
  def startOfDay(dt: LocalDateTime): LocalDateTime = {
    dt.toLocalDate.atStartOfDay
  }
  
  def endOfDay(dt: LocalDateTime): LocalDateTime = {
    dt.toLocalDate.atTime(23, 59, 59)
  }
}

object StringUtils {
  
  def isEmpty(str: String): Boolean = str == null || str.trim.isEmpty
  
  def isNotEmpty(str: String): Boolean = !isEmpty(str)
  
  def containsIgnoreCase(str: String, substring: String): Boolean = {
    str.toLowerCase.contains(substring.toLowerCase)
  }
  
  def removeSpecialChars(str: String): String = {
    str.replaceAll("[^a-zA-Z0-9\\s]", "")
  }
  
  def extractNumbers(str: String): List[Int] = {
    str.replaceAll("\\D+", " ").trim.split("\\s+").filter(_.nonEmpty).map(_.toInt).toList
  }
  
  def maskEmail(email: String): String = {
    val parts = email.split("@")
    if (parts.length == 2) {
      val username = parts(0)
      val domain = parts(1)
      val masked = username.take(2) + "*" * (username.length - 2).max(0)
      s"$masked@$domain"
    } else {
      email
    }
  }
  
  def capitalizeFirst(str: String): String = {
    if (str.isEmpty) str
    else str.head.toUpper + str.tail
  }
  
  def padLeft(str: String, length: Int, char: Char = ' '): String = {
    if (str.length >= length) str
    else char.toString * (length - str.length) + str
  }
  
  def padRight(str: String, length: Int, char: Char = ' '): String = {
    if (str.length >= length) str
    else str + char.toString * (length - str.length)
  }
}

