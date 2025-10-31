## Описание

Этот проект представляет собой комплексное приложение на Scala, которое демонстрирует:

- **Case classes** и паттерн-матчинг
- **Traits** и sealed traits
- **Функциональное программирование** (Option, Try, for-comprehension)
- **Коллекции** и операции над ними
- **Type enrichment** (неявные классы)
- **Сервисный слой** с обработкой ошибок
- **Тестирование** с использованием ScalaTest

## Структура проекта

```
ScalaProject/
├── build.sbt                    # Конфигурация сборки
├── project/
│   └── build.properties         # Версия sbt
├── src/
│   ├── main/
│   │   └── scala/
│   │       └── com/
│   │           └── example/
│   │               ├── Main.scala                    # Точка входа
│   │               ├── models/                      # Модели данных
│   │               │   ├── User.scala
│   │               │   └── Product.scala
│   │               ├── service/                     # Бизнес-логика
│   │               │   ├── UserService.scala
│   │               │   └── ProductService.scala
│   │               ├── functional/                  # Функциональное программирование
│   │               │   └── MonadExample.scala
│   │               ├── pattern/                     # Pattern matching
│   │               │   └── PatternMatchingExample.scala
│   │               └── util/                        # Утилиты
│   │                   └── Extensions.scala
│   └── test/
│       └── scala/
│           └── com/
│               └── example/
│                   ├── models/
│                   │   └── UserTest.scala
│                   ├── service/
│                   │   └── UserServiceTest.scala
│                   └── functional/
│                       └── MonadExampleTest.scala
└── README.md
```

## Требования

- Java JDK 8 или выше
- Scala 2.13.12
- sbt 1.9.7

## Установка и запуск

### Требования для установки

Проект уже настроен с необходимыми инструментами:
- **Java JDK 17** (установлен в `C:\Users\wsr\jdk\jdk-17.0.2`)
- **sbt 1.9.7** (launcher установлен)

Для запуска используйте предоставленный скрипт `sbt.bat` или напрямую:

### Запуск приложения

**Вариант 1 (через скрипт):**
```bash
.\sbt.bat run
```

**Вариант 2 (напрямую):**
```bash
$env:JAVA_HOME = "C:\Users\wsr\jdk\jdk-17.0.2"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
java -Xmx512M -jar "$env:USERPROFILE\.sbt\bin\sbt-launch.jar" run
```

### Запуск тестов

```bash
.\sbt.bat test
```

или

```bash
java -Xmx512M -jar "$env:USERPROFILE\.sbt\bin\sbt-launch.jar" test
```

### Компиляция

```bash
.\sbt.bat compile
```

### Создание JAR файла

```bash
.\sbt.bat package
```

## Основные возможности

### 1. Модели данных

- **User** - модель пользователя с валидацией
- **Product** - модель продукта с sealed trait для статуса

### 2. Сервисы

- **UserService** - управление пользователями (CRUD операции, статистика)
- **ProductService** - управление продуктами (склад, продажи, статусы)

### 3. Функциональное программирование

- Работа с `Option` и `Try`
- For-comprehension для безопасных операций
- Операции над коллекциями (map, filter, flatMap)

### 4. Pattern Matching

- Pattern matching для различных типов данных
- Защитные выражения (guards)
- Работа с case classes и sealed traits

### 5. Расширения

- Неявные классы для обогащения типов
- Расширение функциональности String, Int, List

## Примеры использования

### Создание пользователя

```scala
val user = User.create(1, "Иван Иванов", "ivan@example.com", 25)
```

### Работа с сервисом

```scala
val userService = new UserService()
userService.addUser(user)
val activeUsers = userService.getActiveUsers
```

### Pattern Matching

```scala
product.status match {
  case Active => "В наличии"
  case OutOfStock => "Нет в наличии"
  case Discontinued => "Снят с производства"
}
```

### Функциональное программирование

```scala
val result = for {
  numA <- parseDouble("10")
  numB <- parseDouble("2")
  div <- divide(numA, numB)
} yield div
```

## Тестирование

Проект включает unit-тесты для основных компонентов, написанные с использованием ScalaTest.

## Автор

Создано как демонстрационный проект возможностей Scala.

## Лицензия

Этот проект создан в образовательных целях.

