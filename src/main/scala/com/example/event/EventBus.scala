package com.example.event

import scala.collection.mutable
import scala.util.{Try, Success, Failure}
import com.example.metrics.GlobalMetrics


sealed trait Event {
  def eventType: String
  def timestamp: Long = System.currentTimeMillis()
}

case class UserCreatedEvent(userId: Long, userName: String) extends Event {
  def eventType: String = "user.created"
}

case class UserUpdatedEvent(userId: Long) extends Event {
  def eventType: String = "user.updated"
}

case class UserDeletedEvent(userId: Long) extends Event {
  def eventType: String = "user.deleted"
}

case class ProductCreatedEvent(productId: Long, productName: String) extends Event {
  def eventType: String = "product.created"
}

case class ProductSoldEvent(productId: Long, quantity: Int) extends Event {
  def eventType: String = "product.sold"
}


trait EventHandler[T <: Event] {
  def handle(event: T): Unit
}


case class Subscription[T <: Event](handler: EventHandler[T], priority: Int = 0) {
  def execute(event: Event): Unit = {
    handler.asInstanceOf[EventHandler[Event]].handle(event)
  }
}

type EventMiddleware = Event => Event

class EventBus {
  private val handlers = mutable.Map[String, List[Subscription[_ <: Event]]]()
  private val eventHistory = mutable.ListBuffer[Event]()
  private val middlewares = mutable.ListBuffer[EventMiddleware]()
  private val maxHistorySize = 10000
  private val errorListeners = mutable.ListBuffer[Throwable => Unit]()
  
  def subscribe[T <: Event](eventType: String, priority: Int = 0)(handler: EventHandler[T]): Unit = {
    val currentHandlers = handlers.getOrElse(eventType, List.empty)
    val subscription = Subscription(handler, priority)
    val updated = (currentHandlers :+ subscription).sortBy(_.priority)(Ordering[Int].reverse)
    handlers(eventType) = updated
  }
  
  def addMiddleware(middleware: EventMiddleware): Unit = {
    middlewares += middleware
  }
  
  def onError(listener: Throwable => Unit): Unit = {
    errorListeners += listener
  }
  
  def subscribeOnce[T <: Event](eventType: String, priority: Int = 0)(handler: EventHandler[T]): Unit = {
    val self = this
    val wrapper = new EventHandler[T] {
      def handle(event: T): Unit = {
        try handler.handle(event)
        finally self.unsubscribe(eventType, this)
      }
    }
    subscribe(eventType, priority)(wrapper)
  }
  
  def publish(event: Event): Unit = {
    val processedEvent = middlewares.foldLeft(event) { (e, mw) => mw(e) }
    GlobalMetrics.incrementCounter("events_published", Map("type" -> processedEvent.eventType))
    
    if (eventHistory.size >= maxHistorySize) {
      eventHistory.remove(0)
    }
    eventHistory += processedEvent
    
    handlers.get(processedEvent.eventType).foreach { subscriptions =>
      subscriptions.foreach { subscription =>
        Try {
          subscription.execute(processedEvent)
        } match {
          case Failure(e) =>
            Console.err.println(s"Error handling event ${processedEvent.eventType}: ${e.getMessage}")
            GlobalMetrics.incrementCounter("events_errors", Map("type" -> processedEvent.eventType))
            errorListeners.foreach(cb => Try(cb(e)))
          case Success(_) => 
        }
      }
    }
  }
  
  def publishAsync(event: Event)(implicit ec: scala.concurrent.ExecutionContext): Unit = {
    scala.concurrent.Future { publish(event) }
  }
  
  def unsubscribe(eventType: String, handler: EventHandler[_ <: Event]): Boolean = {
    handlers.get(eventType).exists { subscriptions =>
      val filtered = subscriptions.filterNot(_.handler == handler)
      if (filtered.size != subscriptions.size) {
        handlers(eventType) = filtered
        true
      } else {
        false
      }
    }
  }
  
  def getEventHistory: List[Event] = eventHistory.toList
  
  def getEventsByType(eventType: String): List[Event] = {
    eventHistory.filter(_.eventType == eventType).toList
  }
  
  def getRecentEvents(count: Int): List[Event] = {
    eventHistory.takeRight(count).toList.reverse
  }
  
  def clearHistory(): Unit = {
    eventHistory.clear()
  }
  
  def getEventCount(eventType: String): Int = {
    eventHistory.count(_.eventType == eventType)
  }
  
  def getSubscriberCount(eventType: String): Int = {
    handlers.get(eventType).map(_.size).getOrElse(0)
  }
  
  def getAllEventTypes: Set[String] = {
    eventHistory.map(_.eventType).toSet
  }
}


object GlobalEventBus extends EventBus



