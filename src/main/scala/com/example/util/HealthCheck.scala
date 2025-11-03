package com.example.util

import scala.util.Try

sealed trait HealthStatus {
  def name: String
  def isHealthy: Boolean
}

case object Healthy extends HealthStatus {
  def name: String = "healthy"
  def isHealthy: Boolean = true
}

case object Unhealthy extends HealthStatus {
  def name: String = "unhealthy"
  def isHealthy: Boolean = false
}

case class Degraded(message: String) extends HealthStatus {
  def name: String = "degraded"
  def isHealthy: Boolean = true
}

trait HealthCheck {
  def check(): HealthStatus
  def name: String
}

class CompositeHealthCheck(checks: List[HealthCheck]) extends HealthCheck {
  def name: String = "composite"
  
  def check(): HealthStatus = {
    val results = checks.map(c => (c.name, c.check()))
    val failures = results.filter(!_._2.isHealthy)
    
    if (failures.isEmpty) {
      Healthy
    } else if (failures.length == results.length) {
      Unhealthy
    } else {
      Degraded(s"Some checks failed: ${failures.map(_._1).mkString(", ")}")
    }
  }
  
  def getAllChecks: Map[String, HealthStatus] = {
    checks.map(c => (c.name, c.check())).toMap
  }
}

object HealthCheck {
  def memoryCheck(): HealthCheck = new HealthCheck {
    def name: String = "memory"
    def check(): HealthStatus = {
      val runtime = Runtime.getRuntime
      val maxMemory = runtime.maxMemory()
      val usedMemory = runtime.totalMemory() - runtime.freeMemory()
      val usagePercent = (usedMemory.toDouble / maxMemory) * 100
      
      if (usagePercent > 90) Unhealthy
      else if (usagePercent > 75) Degraded(s"Memory usage: ${usagePercent.toInt}%")
      else Healthy
    }
  }
  
  def diskCheck(minFreeSpaceMB: Long = 100): HealthCheck = new HealthCheck {
    def name: String = "disk"
    def check(): HealthStatus = {
      Try {
        val file = new java.io.File(".")
        val freeSpace = file.getFreeSpace / (1024 * 1024)
        if (freeSpace < minFreeSpaceMB) Unhealthy
        else Healthy
      }.getOrElse(Unhealthy)
    }
  }
  
  def createComposite(checks: HealthCheck*): CompositeHealthCheck = {
    new CompositeHealthCheck(checks.toList)
  }
}


