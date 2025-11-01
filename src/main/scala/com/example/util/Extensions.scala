package com.example.util

/**
 * Расширения типов через implicit classes (Type Enrichment)
 * 
 * Добавляет полезные методы к стандартным типам Scala
 */
object Extensions {
  implicit class RichString(val s: String) extends AnyVal {
    def capitalizeWords: String = {
      s.split("\\s+").map(_.capitalize).mkString(" ")
    }
    
    def isValidEmail: Boolean = {
      val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,}|\\[\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\])$".r
      emailRegex.matches(s) && s.length > 5 && s.length <= 254
    }
    
    def truncate(maxLength: Int): String = {
      if (s.length <= maxLength) s else s.take(maxLength - 3) + "..."
    }
    
    def removeWhitespace: String = s.replaceAll("\\s+", "")
    
    def toSnakeCase: String = {
      s.replaceAll("([A-Z])", "_$1").toLowerCase.stripPrefix("_")
    }
    
    def reverseWords: String = {
      s.split("\\s+").reverse.mkString(" ")
    }
  }
  
  implicit class RichInt(val n: Int) extends AnyVal {
    def isEven: Boolean = n % 2 == 0
    def isOdd: Boolean = n % 2 != 0
    def factorial: BigInt = {
      if (n <= 1) 1
      else (1 to n).map(BigInt(_)).product
    }
    
    def isPrime: Boolean = {
      if (n < 2) false
      else if (n == 2) true
      else !(2 until n).exists(n % _ == 0)
    }
    
    def toBinary: String = n.toBinaryString
    
    def abs: Int = math.abs(n)
    
    def power(exp: Int): Int = {
      if (exp < 0) 0
      else if (exp == 0) 1
      else (1 to exp).foldLeft(1)((acc, _) => acc * n)
    }
  }
  
  implicit class RichList[T](val list: List[T]) extends AnyVal {
    def safeHead: Option[T] = list.headOption
    
    def safeLast: Option[T] = if (list.isEmpty) None else Some(list.last)
    
    def groupByCount: Map[T, Int] = {
      list.groupBy(identity).view.mapValues(_.size).toMap
    }
    
    def takeWhileInclusive(p: T => Boolean): List[T] = {
      list.takeWhile(p) ++ list.dropWhile(p).headOption
    }
    
    def splitAtFirst(p: T => Boolean): (List[T], List[T]) = {
      val index = list.indexWhere(p)
      if (index == -1) (list, Nil)
      else list.splitAt(index + 1)
    }
    
    def removeDuplicates: List[T] = list.distinct
  }
  
  implicit class RichBigDecimal(val bd: BigDecimal) extends AnyVal {
    def roundTo(decimals: Int): BigDecimal = {
      bd.setScale(decimals, BigDecimal.RoundingMode.HALF_UP)
    }
    
    def percentage: BigDecimal = bd * 100
    
    def formatCurrency: String = {
      f"$$${bd.toDouble}%.2f"
    }
  }
}
