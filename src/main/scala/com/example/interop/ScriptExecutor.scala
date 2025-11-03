package com.example.interop

import scala.sys.process._
import scala.util.{Try, Success, Failure}
import java.io.File

sealed trait ScriptLanguage {
  def command: String
  def extension: String
}

case object Python extends ScriptLanguage {
  def command: String = "python"
  def extension: String = ".py"
}

case object JavaScript extends ScriptLanguage {
  def command: String = "node"
  def extension: String = ".js"
}

case object Bash extends ScriptLanguage {
  def command: String = "bash"
  def extension: String = ".sh"
}

case object JavaClass extends ScriptLanguage {
  def command: String = "java"
  def extension: String = ".class"
}

class ScriptExecutor {
  
  def executeScript(language: ScriptLanguage, scriptPath: String, args: List[String] = Nil): Try[String] = {
    Try {
      val cmd = language.command :: scriptPath :: args
      val result = cmd.!!
      result.trim
    }
  }
  
  def executeCode(language: ScriptLanguage, code: String, args: List[String] = Nil): Try[String] = {
    Try {
      val tempFile = File.createTempFile("scala_script_", language.extension)
      try {
        val writer = new java.io.PrintWriter(tempFile)
        writer.write(code)
        writer.close()
        executeScript(language, tempFile.getAbsolutePath, args).get
      } finally {
        tempFile.delete()
      }
    }
  }
  
  def checkLanguageAvailable(language: ScriptLanguage): Boolean = {
    Try {
      language match {
        case Python =>
          val result = (Process("python --version") !!).trim
          result.startsWith("Python")
        case JavaScript =>
          val result = (Process("node --version") !!).trim
          result.startsWith("v")
        case Bash =>
          val result = (Process("bash --version") !!).trim
          result.nonEmpty
        case JavaClass =>
          val result = (Process("java -version") !!).trim
          result.contains("version")
      }
    }.getOrElse(false)
  }
  
  def getAvailableLanguages: List[ScriptLanguage] = {
    List(Python, JavaScript, Bash, JavaClass).filter(checkLanguageAvailable)
  }
}

object GlobalScriptExecutor extends ScriptExecutor

