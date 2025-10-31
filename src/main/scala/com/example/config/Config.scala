package com.example.config

object Config {
  
  case class AppConfig(
    appName: String,
    version: String,
    maxUsers: Int,
    defaultPageSize: Int,
    enableLogging: Boolean,
    databaseUrl: Option[String]
  )
  
  private val defaultConfig = AppConfig(
    appName = "ScalaProject",
    version = "1.0.0",
    maxUsers = 1000,
    defaultPageSize = 20,
    enableLogging = true,
    databaseUrl = None
  )
  
  def fromEnvironment: AppConfig = {
    AppConfig(
      appName = sys.env.getOrElse("APP_NAME", defaultConfig.appName),
      version = sys.env.getOrElse("APP_VERSION", defaultConfig.version),
      maxUsers = sys.env.get("MAX_USERS").map(_.toInt).getOrElse(defaultConfig.maxUsers),
      defaultPageSize = sys.env.get("PAGE_SIZE").map(_.toInt).getOrElse(defaultConfig.defaultPageSize),
      enableLogging = sys.env.get("ENABLE_LOGGING").map(_.toBoolean).getOrElse(defaultConfig.enableLogging),
      databaseUrl = sys.env.get("DATABASE_URL")
    )
  }
  
  lazy val current: AppConfig = fromEnvironment
  
  object Settings {
    val appName: String = current.appName
    val version: String = current.version
    val maxUsers: Int = current.maxUsers
    val defaultPageSize: Int = current.defaultPageSize
    val enableLogging: Boolean = current.enableLogging
    val databaseUrl: Option[String] = current.databaseUrl
  }
}

object ComponentConfig {
  
  case class UserServiceConfig(
    maxRetries: Int = 3,
    timeoutMs: Long = 5000,
    cacheEnabled: Boolean = true
  )
  
  case class ProductServiceConfig(
    lowStockThreshold: Int = 10,
    autoRestock: Boolean = false,
    maxPrice: BigDecimal = BigDecimal(1000000)
  )
  
  val userService: UserServiceConfig = UserServiceConfig()
  val productService: ProductServiceConfig = ProductServiceConfig()
}
