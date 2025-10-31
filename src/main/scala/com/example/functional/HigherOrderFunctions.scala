package com.example.functional

/**
 * Демонстрация функций высшего порядка и композиции функций
 */
object HigherOrderFunctions {
  
  // Функции высшего порядка
  def applyOperation(f: Int => Int, x: Int): Int = f(x)
  
  def twice(f: Int => Int): Int => Int = x => f(f(x))
  
  def compose[A, B, C](f: B => C, g: A => B): A => C = x => f(g(x))
  
  def andThen[A, B, C](f: A => B, g: B => C): A => C = x => g(f(x))
  
  // Примеры использования
  val addOne: Int => Int = _ + 1
  val multiplyByTwo: Int => Int = _ * 2
  val square: Int => Int = x => x * x
  
  // Композиция функций
  val addOneAndDouble: Int => Int = addOne andThen multiplyByTwo
  val doubleAndSquare: Int => Int = multiplyByTwo andThen square
  
  // Функции с частичным применением (currying)
  def multiply(x: Int)(y: Int): Int = x * y
  val multiplyBy10: Int => Int = multiply(10)
  
  // Функции с несколькими параметрами и currying
  def add(x: Int, y: Int, z: Int): Int = x + y + z
  val addWithPartial: (Int, Int) => Int = add(5, _, _)
  
  // Работа с коллекциями через функции высшего порядка
  def processList(numbers: List[Int], op: Int => Int): List[Int] = {
    numbers.map(op)
  }
  
  def filterAndTransform[A, B](
    list: List[A],
    predicate: A => Boolean,
    transform: A => B
  ): List[B] = {
    list.filter(predicate).map(transform)
  }
  
  // Fold операции
  def sum(numbers: List[Int]): Int = {
    numbers.foldLeft(0)(_ + _)
  }
  
  def product(numbers: List[Int]): Int = {
    numbers.foldLeft(1)(_ * _)
  }
  
  def reverse[A](list: List[A]): List[A] = {
    list.foldLeft(List.empty[A])((acc, x) => x :: acc)
  }
  
  // Reduce операции
  def max(numbers: List[Int]): Option[Int] = {
    if (numbers.isEmpty) None
    else Some(numbers.reduce((a, b) => if (a > b) a else b))
  }
  
  // Комбинация операций
  def transformEvenNumbers(numbers: List[Int], f: Int => Int): List[Int] = {
    numbers.map { n =>
      if (n % 2 == 0) f(n) else n
    }
  }
}

