package com.example.typeclass

trait Serializer[T] {
  def serialize(value: T): String
}

object Serializer {
  implicit val intSerializer: Serializer[Int] = (value: Int) => s"Int($value)"
  implicit val stringSerializer: Serializer[String] = (value: String) => s"String($value)"
  implicit val doubleSerializer: Serializer[Double] = (value: Double) => f"Double($value%.2f)"
  
  implicit def listSerializer[T](implicit s: Serializer[T]): Serializer[List[T]] = {
    (list: List[T]) => s"[${list.map(s.serialize).mkString(", ")}]"
  }
  
  implicit class SerializerOps[T](value: T)(implicit serializer: Serializer[T]) {
    def serialize: String = serializer.serialize(value)
  }
}

trait Comparator[T] {
  def compare(a: T, b: T): Int
}

object Comparator {
  implicit val intComparator: Comparator[Int] = (a: Int, b: Int) => a.compareTo(b)
  implicit val stringComparator: Comparator[String] = (a: String, b: String) => a.compareTo(b)
  
  implicit class ComparatorOps[T](a: T)(implicit comparator: Comparator[T]) {
    def <(b: T): Boolean = comparator.compare(a, b) < 0
    def >(b: T): Boolean = comparator.compare(a, b) > 0
    def <=(b: T): Boolean = comparator.compare(a, b) <= 0
    def >=(b: T): Boolean = comparator.compare(a, b) >= 0
  }
}

trait Summable[T] {
  def zero: T
  def add(a: T, b: T): T
}

object Summable {
  implicit val intSummable: Summable[Int] = new Summable[Int] {
    def zero: Int = 0
    def add(a: Int, b: Int): Int = a + b
  }
  
  implicit val stringSummable: Summable[String] = new Summable[String] {
    def zero: String = ""
    def add(a: String, b: String): String = a + b
  }
  
  def sum[T](list: List[T])(implicit summable: Summable[T]): T = {
    list.foldLeft(summable.zero)(summable.add)
  }
}

object TypeClassExample {
  import Serializer._
  import Comparator._
  import Summable._
  
  def serializeValue[T](value: T)(implicit serializer: Serializer[T]): String = {
    serializer.serialize(value)
  }
  
  def maxValue[T](a: T, b: T)(implicit comparator: Comparator[T]): T = {
    if (comparator.compare(a, b) >= 0) a else b
  }
  
  def example(): Unit = {
    println(serializeValue(42))
    println(serializeValue("Hello"))
    println(serializeValue(3.14159))
    println(serializeValue(List(1, 2, 3)))
    
    println(42.serialize)
    println("World".serialize)
    
    println(maxValue(5, 10))
    println(maxValue("abc", "def"))
    
    println(sum(List(1, 2, 3, 4, 5)))
    println(sum(List("a", "b", "c")))
  }
}
