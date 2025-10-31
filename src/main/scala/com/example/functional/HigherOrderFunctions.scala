package com.example.functional

object HigherOrderFunctions {
  
  def applyOperation(f: Int => Int, x: Int): Int = f(x)
  
  def twice(f: Int => Int): Int => Int = x => f(f(x))
  
  def compose[A, B, C](f: B => C, g: A => B): A => C = x => f(g(x))
  
  def andThen[A, B, C](f: A => B, g: B => C): A => C = x => g(f(x))
  
  val addOne: Int => Int = _ + 1
  val multiplyByTwo: Int => Int = _ * 2
  val square: Int => Int = x => x * x
  
  val addOneAndDouble: Int => Int = addOne andThen multiplyByTwo
  val doubleAndSquare: Int => Int = multiplyByTwo andThen square
  
  def multiply(x: Int)(y: Int): Int = x * y
  val multiplyBy10: Int => Int = multiply(10)
  
  def add(x: Int, y: Int, z: Int): Int = x + y + z
  val addWithPartial: (Int, Int) => Int = add(5, _, _)
  
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
  
  def sum(numbers: List[Int]): Int = {
    numbers.foldLeft(0)(_ + _)
  }
  
  def product(numbers: List[Int]): Int = {
    numbers.foldLeft(1)(_ * _)
  }
  
  def reverse[A](list: List[A]): List[A] = {
    list.foldLeft(List.empty[A])((acc, x) => x :: acc)
  }
  
  def max(numbers: List[Int]): Option[Int] = {
    if (numbers.isEmpty) None
    else Some(numbers.reduce((a, b) => if (a > b) a else b))
  }
  
  def transformEvenNumbers(numbers: List[Int], f: Int => Int): List[Int] = {
    numbers.map { n =>
      if (n % 2 == 0) f(n) else n
    }
  }
}
