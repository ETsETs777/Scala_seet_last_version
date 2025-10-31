package com.example.util

object Extensions {
  implicit class RichString(val s: String) extends AnyVal {
    def capitalizeWords: String = {
      s.split("\\s+").map(_.capitalize).mkString(" ")
    }
    
    def isValidEmail: Boolean = {
      s.contains("@") && s.contains(".") && s.length > 5
    }
  }
  
  implicit class RichInt(val n: Int) extends AnyVal {
    def isEven: Boolean = n % 2 == 0
    def isOdd: Boolean = n % 2 != 0
    def factorial: BigInt = {
      if (n <= 1) 1
      else (1 to n).map(BigInt(_)).product
    }
  }
  
  implicit class RichList[T](val list: List[T]) extends AnyVal {
    def safeHead: Option[T] = list.headOption
    
    def safeLast: Option[T] = if (list.isEmpty) None else Some(list.last)
    
    def groupByCount: Map[T, Int] = {
      list.groupBy(identity).view.mapValues(_.size).toMap
    }
  }
}
