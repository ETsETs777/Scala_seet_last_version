package com.example.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

sealed trait LogLevel {
  def name: String
}

case object DEBUG extends LogLevel { def name = "DEBUG" }
case object INFO extends LogLevel { def name = "INFO" }
case object WARN extends LogLevel { def name = "WARN" }
case object ERROR extends LogLevel { def name = "ERROR" }

trait Logger {
  protected val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  
  protected def formatMessage(level: LogLevel, message: String, throwable: Option[Throwable] = None): String = {
    val timestamp = LocalDateTime.now().format(dateFormatter)
    val throwableMsg = throwable.map(t => s"\n${t.getClass.getName}: ${t.getMessage}\n${t.getStackTrace.take(5).mkString("\n")}").getOrElse("")
    s"[$timestamp] [${level.name}] $message$throwableMsg"
  }
  
  def debug(message: String): Unit = log(DEBUG, message)
  def info(message: String): Unit = log(INFO, message)
  def warn(message: String): Unit = log(WARN, message)
  def error(message: String, throwable: Option[Throwable] = None): Unit = log(ERROR, message, throwable)
  
  protected def log(level: LogLevel, message: String, throwable: Option[Throwable] = None): Unit
}

object ConsoleLogger extends Logger {
  override protected def log(level: LogLevel, message: String, throwable: Option[Throwable] = None): Unit = {
    val formatted = formatMessage(level, message, throwable)
    level match {
      case ERROR => Console.err.println(formatted)
      case _ => println(formatted)
    }
  }
}

object FileLogger extends Logger {
  import java.io.{PrintWriter, FileWriter}
  
  private val logFile = "application.log"
  
  override protected def log(level: LogLevel, message: String, throwable: Option[Throwable] = None): Unit = {
    val formatted = formatMessage(level, message, throwable)
    try {
      val writer = new PrintWriter(new FileWriter(logFile, true))
      writer.println(formatted)
      writer.close()
    } catch {
      case e: Exception => Console.err.println(s"Failed to write to log file: ${e.getMessage}")
    }
  }
}

object CompositeLogger extends Logger {
  private val loggers = List(ConsoleLogger, FileLogger)
  
  override protected def log(level: LogLevel, message: String, throwable: Option[Throwable] = None): Unit = {
    loggers.foreach(_.log(level, message, throwable))
  }
}

