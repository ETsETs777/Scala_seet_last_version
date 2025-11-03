package com.example.util

sealed trait Result[+T, +E] {
  def isSuccess: Boolean
  def isFailure: Boolean = !isSuccess
  def get: T
  def getOrElse[U >: T](default: => U): U
  def map[U](f: T => U): Result[U, E]
  def flatMap[U, F >: E](f: T => Result[U, F]): Result[U, F]
  def fold[U](failure: E => U, success: T => U): U
}

case class Success[T](value: T) extends Result[T, Nothing] {
  def isSuccess: Boolean = true
  def get: T = value
  def getOrElse[U >: T](default: => U): U = value
  def map[U](f: T => U): Result[U, Nothing] = Success(f(value))
  def flatMap[U, F](f: T => Result[U, F]): Result[U, F] = f(value)
  def fold[U](failure: Nothing => U, success: T => U): U = success(value)
}

case class Failure[E](error: E) extends Result[Nothing, E] {
  def isSuccess: Boolean = false
  def get: Nothing = throw new NoSuchElementException("Failure.get")
  def getOrElse[U](default: => U): U = default
  def map[U](f: Nothing => U): Result[U, E] = this
  def flatMap[U, F >: E](f: Nothing => Result[U, F]): Result[U, F] = this
  def fold[U](failure: E => U, success: Nothing => U): U = failure(error)
}

object Result {
  def fromOption[T, E](opt: Option[T], error: => E): Result[T, E] = {
    opt.map(Success.apply).getOrElse(Failure(error))
  }
  
  def fromTry[T](tryValue: scala.util.Try[T]): Result[T, Throwable] = {
    tryValue match {
      case scala.util.Success(value) => Success(value)
      case scala.util.Failure(error) => Failure(error)
    }
  }
  
  def sequence[T, E](results: List[Result[T, E]]): Result[List[T], E] = {
    results.foldRight(Success(List.empty[T]): Result[List[T], E]) {
      case (Success(value), Success(acc)) => Success(value :: acc)
      case (Failure(error), _) => Failure(error)
      case (_, Failure(error)) => Failure(error)
    }
  }
  
  def traverse[A, B, E](list: List[A])(f: A => Result[B, E]): Result[List[B], E] = {
    sequence(list.map(f))
  }
}


