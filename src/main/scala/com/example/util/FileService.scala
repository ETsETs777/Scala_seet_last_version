package com.example.util

import java.io.{File, PrintWriter, FileWriter, BufferedReader, FileReader}
import scala.util.{Try, Success, Failure}

/**
 * Сервис для работы с файлами
 */
class FileService {
  
  /**
   * Записывает строку в файл
   */
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
  
  /**
   * Читает содержимое файла
   */
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
  
  /**
   * Записывает список строк в файл
   */
  def writeLines(path: String, lines: List[String]): Try[Unit] = {
    writeFile(path, lines.mkString("\n"))
  }
  
  /**
   * Читает файл построчно
   */
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
  
  /**
   * Проверяет существование файла
   */
  def fileExists(path: String): Boolean = {
    new File(path).exists()
  }
  
  /**
   * Удаляет файл
   */
  def deleteFile(path: String): Try[Unit] = Try {
    val file = new File(path)
    if (file.exists()) {
      file.delete()
    }
  }
  
  /**
   * Получает размер файла в байтах
   */
  def getFileSize(path: String): Try[Long] = Try {
    val file = new File(path)
    if (!file.exists()) {
      throw new IllegalArgumentException(s"File does not exist: $path")
    }
    file.length()
  }
  
  /**
   * Копирует файл
   */
  def copyFile(source: String, destination: String): Try[Unit] = {
    readFile(source).flatMap { content =>
      writeFile(destination, content)
    }
  }
}

