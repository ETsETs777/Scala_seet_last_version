package com.example.util

object Localization {
  private val messages: Map[String, Map[String, String]] = Map(
    "en" -> Map(
      "app.title" -> "Scala Features Demo",
      "app.done" -> "Demo finished!",
      "section.users" -> "Users",
      "section.products" -> "Products",
      "section.metrics" -> "Metrics",
      "section.async" -> "Async Operations",
      "section.health" -> "Health Checks",
      "health.ok" -> "ok"
    ),
    "ru" -> Map(
      "app.title" -> "Демонстрация возможностей Scala",
      "app.done" -> "Демонстрация завершена!",
      "section.users" -> "Работа с пользователями",
      "section.products" -> "Работа с продуктами",
      "section.metrics" -> "Метрики",
      "section.async" -> "Асинхронные операции",
      "section.health" -> "Проверки состояния",
      "health.ok" -> "ok"
    ),
    "es" -> Map(
      "app.title" -> "Demostración de Scala",
      "app.done" -> "¡Demostración terminada!",
      "section.users" -> "Usuarios",
      "section.products" -> "Productos",
      "section.metrics" -> "Métricas",
      "section.async" -> "Operaciones asíncronas",
      "section.health" -> "Comprobaciones de salud",
      "health.ok" -> "ok"
    )
  )

  private val defaultLang = "ru"

  def t(lang: String, key: String): String = {
    val l = normalize(lang)
    messages.getOrElse(l, messages(defaultLang)).getOrElse(key, key)
  }

  def detect(acceptLanguageHeader: Option[String]): String = {
    acceptLanguageHeader.flatMap { header =>
      val parts = header.split(",").toList.map(_.trim).filter(_.nonEmpty)
      parts.headOption.map(normalize)
    }.getOrElse(defaultLang)
  }

  private def normalize(lang: String): String = {
    val code = lang.toLowerCase.take(2)
    if (messages.contains(code)) code else defaultLang
  }
}
