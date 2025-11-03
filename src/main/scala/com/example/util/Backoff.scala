package com.example.util

import scala.concurrent.duration.Duration

sealed trait BackoffStrategy {
  def nextDelay(attempt: Int): Duration
}

case class LinearBackoff(initialDelay: Duration, increment: Duration) extends BackoffStrategy {
  def nextDelay(attempt: Int): Duration = {
    initialDelay + (increment * attempt)
  }
}

case class ExponentialBackoff(initialDelay: Duration, multiplier: Double = 2.0, maxDelay: Option[Duration] = None) extends BackoffStrategy {
  def nextDelay(attempt: Int): Duration = {
    val delay = initialDelay * math.pow(multiplier, attempt)
    maxDelay.map(md => if (delay > md) md else delay).getOrElse(delay)
  }
}

case class ConstantBackoff(delay: Duration) extends BackoffStrategy {
  def nextDelay(attempt: Int): Duration = delay
}

object Backoff {
  def retry[T](operation: => T, maxRetries: Int = 3, strategy: BackoffStrategy = ExponentialBackoff(scala.concurrent.duration.Duration(100, scala.concurrent.duration.MILLISECONDS))): T = {
    def attempt(retriesLeft: Int): T = {
      try {
        operation
      } catch {
        case e: Exception if retriesLeft > 0 =>
          val attemptNumber = maxRetries - retriesLeft
          val delay = strategy.nextDelay(attemptNumber)
          Thread.sleep(delay.toMillis)
          attempt(retriesLeft - 1)
        case e: Exception => throw e
      }
    }
    attempt(maxRetries)
  }
}


