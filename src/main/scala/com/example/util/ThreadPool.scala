package com.example.util

import java.util.concurrent.{Executors, ThreadFactory, TimeUnit}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object ThreadPool {
  
  def createFixedPool(size: Int, namePrefix: String = "thread"): ExecutionContextExecutor = {
    val factory = new ThreadFactory {
      private var counter = 0
      def newThread(r: Runnable): Thread = {
        counter += 1
        val thread = new Thread(r, s"$namePrefix-$counter")
        thread.setDaemon(true)
        thread
      }
    }
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(size, factory))
  }
  
  def createCachedPool(namePrefix: String = "thread"): ExecutionContextExecutor = {
    val factory = new ThreadFactory {
      private var counter = 0
      def newThread(r: Runnable): Thread = {
        counter += 1
        val thread = new Thread(r, s"$namePrefix-$counter")
        thread.setDaemon(true)
        thread
      }
    }
    ExecutionContext.fromExecutor(Executors.newCachedThreadPool(factory))
  }
  
  def shutdownPool(ec: ExecutionContextExecutor): Unit = {
    ec match {
      case ece: ExecutionContextExecutorService if ece.isInstanceOf[ExecutionContextExecutorService] =>
        val service = ece.asInstanceOf[java.util.concurrent.ExecutorService]
        service.shutdown()
        try {
          if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
            service.shutdownNow()
          }
        } catch {
          case _: InterruptedException =>
            service.shutdownNow()
            Thread.currentThread().interrupt()
        }
      case _ =>
    }
  }
  
  private type ExecutionContextExecutorService = ExecutionContextExecutor with java.util.concurrent.ExecutorService
}


