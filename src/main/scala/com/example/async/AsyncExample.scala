package com.example.async

import scala.concurrent.{Future, ExecutionContext, Promise}
import scala.util.{Success, Failure, Try}
import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent.duration._

object AsyncExample {
  
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))
  
  def retry[T](operation: => Future[T], maxRetries: Int = 3, delay: Duration = 1.second): Future[T] = {
    import com.example.util.Backoff
    import Backoff.ExponentialBackoff
    
    val strategy = ExponentialBackoff(delay, 2.0, Some(10.seconds))
    def attempt(retriesLeft: Int): Future[T] = {
      operation.recoverWith {
        case e if retriesLeft > 0 =>
          val attemptNumber = maxRetries - retriesLeft
          val backoffDelay = strategy.nextDelay(attemptNumber)
          Thread.sleep(backoffDelay.toMillis)
          attempt(retriesLeft - 1)
        case e => Future.failed(e)
      }
    }
    attempt(maxRetries)
  }
  
  def withTimeout[A](future: Future[A], timeout: Duration): Future[Option[A]] = {
    val promise = Promise[Option[A]]()
    val timer = new java.util.Timer()
    timer.schedule(new java.util.TimerTask {
      def run(): Unit = {
        promise.trySuccess(None)
        timer.cancel()
      }
    }, timeout.toMillis)
    
    future.onComplete {
      case Success(value) =>
        promise.trySuccess(Some(value))
        timer.cancel()
      case Failure(e) =>
        promise.tryFailure(e)
        timer.cancel()
    }
    
    promise.future
  }
  
  def firstCompleted[A](futures: List[Future[A]]): Future[A] = {
    val promise = Promise[A]()
    futures.foreach(_.onComplete {
      case Success(value) => promise.trySuccess(value)
      case Failure(e) => if (!promise.isCompleted) promise.tryFailure(e)
    })
    promise.future
  }
  
  def batchProcess[A, B](items: List[A], batchSize: Int, processor: A => Future[B]): Future[List[B]] = {
    val batches = items.grouped(batchSize).toList
    val batchFutures = batches.map(batch => Future.sequence(batch.map(processor)))
    Future.sequence(batchFutures).map(_.flatten)
  }
  
  def circuitBreaker[T](operation: => Future[T], failureThreshold: Int = 5, timeout: Duration = 10.seconds): Future[T] = {
    var failureCount = 0
    var lastFailureTime = 0L
    
    def isOpen: Boolean = {
      val now = System.currentTimeMillis()
      if (now - lastFailureTime > timeout.toMillis) {
        failureCount = 0
        false
      } else {
        failureCount >= failureThreshold
      }
    }
    
    if (isOpen) {
      Future.failed(new RuntimeException("Circuit breaker is open"))
    } else {
      operation.andThen {
        case Failure(_) =>
          failureCount += 1
          lastFailureTime = System.currentTimeMillis()
        case Success(_) =>
          failureCount = 0
      }
    }
  }
  
  def parallelMap[A, B](items: List[A], fn: A => Future[B]): Future[List[B]] = {
    Future.sequence(items.map(fn))
  }
  
  def sequentialMap[A, B](items: List[A], fn: A => Future[B]): Future[List[B]] = {
    items.foldLeft(Future.successful(List.empty[B])) { (acc, item) =>
      for {
        prev <- acc
        next <- fn(item)
      } yield prev :+ next
    }
  }
  
  def asyncCalculation(x: Int): Future[Int] = Future {
    Thread.sleep(100)
    x * 2
  }
  
  def asyncStringProcessing(s: String): Future[String] = Future {
    Thread.sleep(50)
    s.toUpperCase
  }
  
  def processData(x: Int): Future[String] = {
    asyncCalculation(x).map(result => s"Result: $result")
  }
  
  def combineAsyncOperations(x: Int, y: Int): Future[Int] = {
    for {
      result1 <- asyncCalculation(x)
      result2 <- asyncCalculation(y)
    } yield result1 + result2
  }
  
  def safeAsyncOperation(x: Int): Future[Int] = Future {
    if (x < 0) throw new IllegalArgumentException("Negative number")
    x * 2
  }.recover {
    case _: IllegalArgumentException => 0
    case _: Exception => -1
  }
  
  def combineMultipleFutures(numbers: List[Int]): Future[List[Int]] = {
    val futures = numbers.map(asyncCalculation)
    Future.sequence(futures)
  }
  
  

  def firstSuccess[A](futures: List[Future[A]]): Future[Option[A]] = {
    Future.sequence(futures).map(_.headOption)
  }
  
  
  def chainOperations(x: Int): Future[String] = {
    asyncCalculation(x)
      .map(_ + 10)
      .map(_.toString)
      .recover {
        case e: Exception => s"Error: ${e.getMessage}"
      }
  }
  
  def example(): Unit = {
    val future1 = asyncCalculation(5)
    future1.onComplete {
      case Success(value) => println(s"Calculation result: $value")
      case Failure(exception) => println(s"Error: ${exception.getMessage}")
    }
    
    val future2 = combineAsyncOperations(3, 4)
    future2.foreach(result => println(s"Combined result: $result"))
    
    val future3 = combineMultipleFutures(List(1, 2, 3, 4, 5))
    future3.foreach(results => println(s"All results: $results"))
  }
}
