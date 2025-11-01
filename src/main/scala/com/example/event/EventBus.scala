package com.example.event

import scala.collection.mutable
import scala.util.{Try, Success, Failure}


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


class EventBus {
  private val handlers = mutable.Map[String, List[EventHandler[_ <: Event]]]()
  private val eventHistory = mutable.ListBuffer[Event]()
  
  
  def subscribe[T <: Event](eventType: String)(handler: EventHandler[T]): Unit = {
    val currentHandlers = handlers.getOrElse(eventType, List.empty)
    handlers(eventType) = currentHandlers :+ handler.asInstanceOf[EventHandler[_ <: Event]]
  }
  
  
  def publish(event: Event): Unit = {
    eventHistory += event
    handlers.get(event.eventType).foreach { eventHandlers =>
      eventHandlers.foreach { handler =>
        Try {
          handler.asInstanceOf[EventHandler[Event]].handle(event)
        } match {
          case Failure(e) =>
            Console.err.println(s"Error handling event ${event.eventType}: ${e.getMessage}")
          case Success(_) => 
        }
      }
    }
  }
  
  
  def getEventHistory: List[Event] = eventHistory.toList
  
  
  def getEventsByType(eventType: String): List[Event] = {
    eventHistory.filter(_.eventType == eventType).toList
  }
  
  
  def clearHistory(): Unit = {
    eventHistory.clear()
  }
  
  
  def getEventCount(eventType: String): Int = {
    eventHistory.count(_.eventType == eventType)
  }
}


object GlobalEventBus extends EventBus

