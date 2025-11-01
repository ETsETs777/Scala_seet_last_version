package com.example.metrics

import scala.collection.mutable
import java.time.LocalDateTime

/**
 * Тип метрики
 */
sealed trait Metric {
  def name: String
  def timestamp: LocalDateTime = LocalDateTime.now()
}

case class Counter(name: String, value: Long) extends Metric
case class Gauge(name: String, value: Double) extends Metric
case class Timer(name: String, durationMs: Long) extends Metric

/**
 * Счетчик метрик
 */
class MetricsCollector {
  private val counters = mutable.Map[String, Long]().withDefaultValue(0L)
  private val gauges = mutable.Map[String, Double]()
  private val timers = mutable.ListBuffer[(String, Long)]()
  
  /**
   * Инкрементирует счетчик
   */
  def incrementCounter(name: String, by: Long = 1L): Unit = {
    counters(name) += by
  }
  
  /**
   * Устанавливает значение gauge
   */
  def setGauge(name: String, value: Double): Unit = {
    gauges(name) = value
  }
  
  /**
   * Записывает время выполнения операции
   */
  def recordTimer(name: String, durationMs: Long): Unit = {
    timers += ((name, durationMs))
  }
  
  /**
   * Измеряет время выполнения блока кода
   */
  def time[T](name: String)(block: => T): T = {
    val start = System.currentTimeMillis()
    try {
      block
    } finally {
      val duration = System.currentTimeMillis() - start
      recordTimer(name, duration)
    }
  }
  
  /**
   * Получает значение счетчика
   */
  def getCounter(name: String): Long = counters(name)
  
  /**
   * Получает значение gauge
   */
  def getGauge(name: String): Option[Double] = gauges.get(name)
  
  /**
   * Получает среднее время для таймера
   */
  def getAverageTimer(name: String): Option[Double] = {
    val timerValues = timers.filter(_._1 == name).map(_._2)
    if (timerValues.nonEmpty) {
      Some(timerValues.sum.toDouble / timerValues.size)
    } else {
      None
    }
  }
  
  /**
   * Получает все метрики в виде строки
   */
  def getMetricsSummary: String = {
    val counterStr = counters.map { case (k, v) => s"Counter[$k] = $v" }.mkString("\n")
    val gaugeStr = gauges.map { case (k, v) => s"Gauge[$k] = $v" }.mkString("\n")
    val timerStr = timers.groupBy(_._1).map { case (k, v) =>
      val avg = v.map(_._2).sum.toDouble / v.size
      s"Timer[$k] avg = ${avg}ms (count = ${v.size})"
    }.mkString("\n")
    
    s"""Metrics Summary:
      |Counters:
      |$counterStr
      |
      |Gauges:
      |$gaugeStr
      |
      |Timers:
      |$timerStr""".stripMargin
  }
  
  /**
   * Сбрасывает все метрики
   */
  def reset(): Unit = {
    counters.clear()
    gauges.clear()
    timers.clear()
  }
}

/**
 * Глобальный сборщик метрик
 */
object GlobalMetrics extends MetricsCollector

