package com.example.util

import java.io.{File, PrintWriter, FileWriter, BufferedReader, FileReader}
import scala.util.{Try, Success, Failure}


class FileService {
  
  
  def writeFile(path: String, content: String): Try[Unit] = Try {
    val file = new File(path)
    file.getParentFile.mkdirs()
    val writer = new PrintWriter(new FileWriter(file))
    try {
      writer.write(content)
    } finally {
      writer.close()
    }
  }
  
  
  def readFile(path: String): Try[String] = Try {
    val file = new File(path)
    if (!file.exists()) {
      throw new IllegalArgumentException(s"File does not exist: $path")
    }
    val reader = new BufferedReader(new FileReader(file))
    try {
      val lines = Iterator.continually(reader.readLine()).takeWhile(_ != null)
      lines.mkString("\n")
    } finally {
      reader.close()
    }
  }
  
  
  def writeLines(path: String, lines: List[String]): Try[Unit] = {
    writeFile(path, lines.mkString("\n"))
  }
  
  
  def readLines(path: String): Try[List[String]] = Try {
    val file = new File(path)
    if (!file.exists()) {
      throw new IllegalArgumentException(s"File does not exist: $path")
    }
    val reader = new BufferedReader(new FileReader(file))
    try {
      Iterator.continually(reader.readLine())
        .takeWhile(_ != null)
        .toList
    } finally {
      reader.close()
    }
  }
  
  
  def fileExists(path: String): Boolean = {
    new File(path).exists()
  }
  
  
  def deleteFile(path: String): Try[Unit] = Try {
    val file = new File(path)
    if (file.exists()) {
      file.delete()
    }
  }
  
  
  def getFileSize(path: String): Try[Long] = Try {
    val file = new File(path)
    if (!file.exists()) {
      throw new IllegalArgumentException(s"File does not exist: $path")
    }
    file.length()
  }
  
  
  def copyFile(source: String, destination: String): Try[Unit] = {
    readFile(source).flatMap { content =>
      writeFile(destination, content)
    }
  }
}

