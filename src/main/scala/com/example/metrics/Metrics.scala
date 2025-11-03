package com.example.metrics

import scala.collection.mutable
import java.time.LocalDateTime


sealed trait Metric {
  def name: String
  def timestamp: LocalDateTime = LocalDateTime.now()
}

case class Counter(name: String, value: Long) extends Metric
case class Gauge(name: String, value: Double) extends Metric
case class Timer(name: String, durationMs: Long) extends Metric


class MetricsCollector {
  private val counters = mutable.Map[String, Long]().withDefaultValue(0L)
  private val gauges = mutable.Map[String, Double]()
  private val timers = mutable.ListBuffer[(String, Long)]()
  private val observations = mutable.Map[String, mutable.ListBuffer[Double]]()
  
  private def labelKey(labels: Map[String, String]): String = {
    if (labels.isEmpty) ""
    else labels.toSeq.sortBy(_._1).map { case (k, v) => s"$k=$v" }.mkString("{", ",", "}")
  }
  
  
  def incrementCounter(name: String, by: Long = 1L): Unit = counters(name) += by
  def incrementCounter(name: String, labels: Map[String, String], by: Long): Unit = {
    val key = s"$name${labelKey(labels)}"
    counters(key) = counters.getOrElse(key, 0L) + by
  }
  def incrementCounter(name: String, labels: Map[String, String] = Map.empty): Unit = incrementCounter(name, labels, 1L)
  
  
  def setGauge(name: String, value: Double): Unit = gauges(name) = value
  def setGauge(name: String, labels: Map[String, String], value: Double): Unit = {
    val key = s"$name${labelKey(labels)}"
    gauges(key) = value
  }
  
  
  def recordTimer(name: String, durationMs: Long): Unit = timers += ((name, durationMs))
  def observe(name: String, value: Double): Unit = {
    val buf = observations.getOrElseUpdate(name, mutable.ListBuffer.empty[Double])
    buf += value
  }
  
  
  def time[T](name: String)(block: => T): T = {
    val start = System.currentTimeMillis()
    try {
      block
    } finally {
      val duration = System.currentTimeMillis() - start
      recordTimer(name, duration)
    }
  }
  
  
  def getCounter(name: String): Long = counters(name)
  
  
  def getGauge(name: String): Option[Double] = gauges.get(name)
  
  
  def getAverageTimer(name: String): Option[Double] = {
    val timerValues = timers.filter(_._1 == name).map(_._2)
    if (timerValues.nonEmpty) {
      Some(timerValues.sum.toDouble / timerValues.size)
    } else {
      None
    }
  }
  
  def getTimerPercentile(name: String, percentile: Double): Option[Double] = {
    val values = timers.filter(_._1 == name).map(_._2.toDouble).sorted
    if (values.isEmpty) None
    else {
      val idx = math.ceil(percentile * values.size).toInt - 1
      Some(values(math.max(0, math.min(values.size - 1, idx))))
    }
  }
  
  
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
  
  def exportPrometheus: String = {
    val sb = new StringBuilder
    counters.foreach { case (k, v) =>
      sb.append(s"# TYPE ${k.replaceAll("[{}=,]", "_")} counter\n")
      sb.append(s"$k $v\n")
    }
    gauges.foreach { case (k, v) =>
      sb.append(s"# TYPE ${k.replaceAll("[{}=,]", "_")} gauge\n")
      sb.append(s"$k $v\n")
    }
    val groupedTimers = timers.groupBy(_._1)
    groupedTimers.foreach { case (name, vals) =>
      val sum = vals.map(_._2.toDouble).sum
      val count = vals.size
      val p50 = getTimerPercentile(name, 0.5).getOrElse(0.0)
      val p90 = getTimerPercentile(name, 0.9).getOrElse(0.0)
      val p99 = getTimerPercentile(name, 0.99).getOrElse(0.0)
      sb.append(s"# TYPE ${name}_summary summary\n")
      sb.append(s"${name}_sum $sum\n")
      sb.append(s"${name}_count $count\n")
      sb.append(s"${name}_quantile{quantile=\"0.5\"} $p50\n")
      sb.append(s"${name}_quantile{quantile=\"0.9\"} $p90\n")
      sb.append(s"${name}_quantile{quantile=\"0.99\"} $p99\n")
    }
    observations.foreach { case (name, vals) =>
      val sum = vals.sum
      val count = vals.size
      sb.append(s"# TYPE ${name}_hist summary\n")
      sb.append(s"${name}_sum $sum\n")
      sb.append(s"${name}_count $count\n")
    }
    sb.toString()
  }
  
  
  def reset(): Unit = {
    counters.clear()
    gauges.clear()
    timers.clear()
  }
}


object GlobalMetrics extends MetricsCollector




