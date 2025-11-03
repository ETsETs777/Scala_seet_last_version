package com.example.event

import org.scalatest.funsuite.AnyFunSuite
import scala.concurrent.{Await, Promise}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class EventBusTest extends AnyFunSuite {
  test("publish delivers events to subscribers in priority order") {
    val bus = new EventBus
    val order = scala.collection.mutable.ListBuffer[String]()
    val h1 = new EventHandler[Event] { def handle(e: Event): Unit = order += "low" }
    val h2 = new EventHandler[Event] { def handle(e: Event): Unit = order += "high" }
    bus.subscribe("user.created", priority = 0)(h1)
    bus.subscribe("user.created", priority = 10)(h2)
    bus.publish(UserCreatedEvent(1, "A"))
    assert(order.toList == List("high", "low"))
  }

  test("subscribeOnce unsubscribes after first delivery") {
    val bus = new EventBus
    var counter = 0
    val h = new EventHandler[Event] { def handle(e: Event): Unit = counter += 1 }
    bus.subscribeOnce("user.created")(h)
    bus.publish(UserCreatedEvent(1, "A"))
    bus.publish(UserCreatedEvent(2, "B"))
    assert(counter == 1)
  }

  test("middleware transforms events before delivery") {
    val bus = new EventBus
    val received = scala.collection.mutable.ListBuffer[Event]()
    val h = new EventHandler[Event] { def handle(e: Event): Unit = received += e }
    bus.addMiddleware { e => e match {
      case UserCreatedEvent(id, name) => UserCreatedEvent(id + 10, name.toUpperCase)
      case other => other
    }}
    bus.subscribe("user.created")(h)
    bus.publish(UserCreatedEvent(1, "john"))
    assert(received.headOption.contains(UserCreatedEvent(11, "JOHN")))
  }

  test("error listeners are notified when handler throws") {
    val bus = new EventBus
    val errorPromise = Promise[Throwable]()
    bus.onError { ex => if (!errorPromise.isCompleted) errorPromise.success(ex) }
    val bad = new EventHandler[Event] { def handle(e: Event): Unit = throw new RuntimeException("boom") }
    bus.subscribe("user.created")(bad)
    bus.publish(UserCreatedEvent(1, "x"))
    val ex = Await.result(errorPromise.future, 2.seconds)
    assert(ex.getMessage.contains("boom"))
  }

  test("publishAsync delivers event asynchronously") {
    val bus = new EventBus
    val p = Promise[Event]()
    val h = new EventHandler[Event] { def handle(e: Event): Unit = p.trySuccess(e) }
    bus.subscribe("user.created")(h)
    bus.publishAsync(UserCreatedEvent(1, "x"))
    val e = Await.result(p.future, 2.seconds)
    assert(e.asInstanceOf[UserCreatedEvent].userId == 1)
  }

  test("unsubscribe removes handler") {
    val bus = new EventBus
    var counter = 0
    val h = new EventHandler[Event] { def handle(e: Event): Unit = counter += 1 }
    bus.subscribe("user.created")(h)
    assert(bus.unsubscribe("user.created", h))
    bus.publish(UserCreatedEvent(1, "A"))
    assert(counter == 0)
  }

  test("history APIs work as expected") {
    val bus = new EventBus
    bus.publish(UserCreatedEvent(1, "A"))
    bus.publish(UserUpdatedEvent(1))
    bus.publish(ProductCreatedEvent(1, "P"))
    assert(bus.getEventHistory.size == 3)
    assert(bus.getEventsByType("user.created").size == 1)
    assert(bus.getRecentEvents(2).size == 2)
    assert(bus.getAllEventTypes == Set("user.created", "user.updated", "product.created"))
    bus.clearHistory()
    assert(bus.getEventHistory.isEmpty)
  }
}
