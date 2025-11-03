package com.example.interop

import scala.util.{Try, Success, Failure}
import com.example.models.{User, Product}

object DataProcessor {
  
  def calculateUserStatisticsWithPython(users: List[User]): Try[Map[String, Any]] = {
    if (users.isEmpty) {
      return Success(Map("error" -> "No users provided"))
    }
    
    val ages = users.map(_.age)
    val pythonCode = s"""
import statistics

ages = [${ages.mkString(", ")}]
active_count = ${users.count(_.isActive)}
total_count = ${users.length}

stats = {
    "mean_age": statistics.mean(ages),
    "median_age": statistics.median(ages),
    "min_age": min(ages),
    "max_age": max(ages),
    "active_users": active_count,
    "total_users": total_count,
    "active_percentage": round((active_count / total_count) * 100, 2) if total_count > 0 else 0
}

print(f"MEAN={stats['mean_age']}")
print(f"MEDIAN={stats['median_age']}")
print(f"MIN={stats['min_age']}")
print(f"MAX={stats['max_age']}")
print(f"ACTIVE={stats['active_users']}")
print(f"TOTAL={stats['total_users']}")
print(f"PERCENTAGE={stats['active_percentage']}")
"""
    GlobalScriptExecutor.executeCode(Python, pythonCode).map { output =>
      parsePythonStats(output)
    }
  }
  
  def calculateProductStatisticsWithPython(products: List[Product]): Try[Map[String, Any]] = {
    if (products.isEmpty) {
      return Success(Map("error" -> "No products provided"))
    }
    
    val prices = products.map(_.price.toDouble)
    val quantities = products.map(_.quantity)
    val pythonCode = s"""
import statistics

prices = [${prices.mkString(", ")}]
quantities = [${quantities.mkString(", ")}]
total_value = ${products.map(_.totalValue.toDouble).sum}

stats = {
    "mean_price": statistics.mean(prices),
    "median_price": statistics.median(prices),
    "min_price": min(prices),
    "max_price": max(prices),
    "total_products": ${products.length},
    "total_inventory_value": total_value,
    "avg_quantity": statistics.mean(quantities),
    "total_quantity": sum(quantities)
}

print(f"MEAN_PRICE={stats['mean_price']}")
print(f"MEDIAN_PRICE={stats['median_price']}")
print(f"MIN_PRICE={stats['min_price']}")
print(f"MAX_PRICE={stats['max_price']}")
print(f"TOTAL={stats['total_products']}")
print(f"VALUE={stats['total_inventory_value']}")
print(f"AVG_QTY={stats['avg_quantity']}")
print(f"SUM_QTY={stats['total_quantity']}")
"""
    GlobalScriptExecutor.executeCode(Python, pythonCode).map { output =>
      parsePythonStats(output)
    }
  }
  
  def calculateDiscountWithPython(price: BigDecimal, discountPercent: Double): Try[BigDecimal] = {
    val pythonCode = s"""
price = ${price.toDouble}
discount = $discountPercent / 100
final_price = price * (1 - discount)
print(f"FINAL={final_price}")
"""
    GlobalScriptExecutor.executeCode(Python, pythonCode).map { output =>
      val finalPrice = output.split("\n").find(_.startsWith("FINAL="))
        .map(_.split("=")(1).toDouble)
        .getOrElse(price.toDouble)
      BigDecimal(finalPrice)
    }
  }
  
  def formatUsersAsJsonWithJavaScript(users: List[User]): Try[String] = {
    val jsCode = s"""
const users = ${users.map(u => s"""{
  id: ${u.id},
  name: "${u.name}",
  email: "${u.email}",
  age: ${u.age},
  isActive: ${u.isActive}
}""").mkString("[", ",", "]")};

const formatted = JSON.stringify(users, null, 2);
console.log(formatted);
"""
    GlobalScriptExecutor.executeCode(JavaScript, jsCode)
  }
  
  def formatProductsAsJsonWithJavaScript(products: List[Product]): Try[String] = {
    val jsCode = s"""
const products = ${products.map(p => s"""{
  id: ${p.id},
  name: "${p.name}",
  price: ${p.price.toDouble},
  quantity: ${p.quantity},
  status: "${p.status}",
  totalValue: ${p.totalValue.toDouble}
}""").mkString("[", ",", "]")};

const formatted = JSON.stringify(products, null, 2);
console.log(formatted);
"""
    GlobalScriptExecutor.executeCode(JavaScript, jsCode)
  }
  
  def validateEmailWithJavaScript(email: String): Try[Boolean] = {
    val jsCode = s"""
const email = "$email";
const pattern = /^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$/;
const isValid = pattern.test(email);
console.log(isValid ? "VALID" : "INVALID");
"""
    GlobalScriptExecutor.executeCode(JavaScript, jsCode).map { output =>
      output.trim.toUpperCase.contains("VALID")
    }
  }
  
  def groupUsersByAgeRangeWithPython(users: List[User], ranges: List[(Int, Int)]): Try[Map[String, Int]] = {
    val ages = users.map(_.age)
    val rangeStr = ranges.map(r => s"(${r._1}, ${r._2})").mkString("[", ",", "]")
    val pythonCode = s"""
ages = [${ages.mkString(", ")}]
ranges = $rangeStr

result = {}
for r_min, r_max in ranges:
    key = f"{r_min}-{r_max}"
    count = sum(1 for age in ages if r_min <= age <= r_max)
    result[key] = count

for key, value in result.items():
    print(f"{key}={value}")
"""
    GlobalScriptExecutor.executeCode(Python, pythonCode).map { output =>
      output.split("\n").filter(_.contains("=")).map { line =>
        val parts = line.split("=")
        parts(0) -> parts(1).toInt
      }.toMap
    }
  }
  
  def calculatePriceDistributionWithPython(products: List[Product], bins: Int = 10): Try[Map[String, Int]] = {
    val prices = products.map(_.price.toDouble)
    val pythonCode = s"""
prices = [${prices.mkString(", ")}]
bins = $bins

if len(prices) == 0:
    print("EMPTY")
else:
    min_price = min(prices)
    max_price = max(prices)
    bin_width = (max_price - min_price) / bins if max_price > min_price else 1
    
    distribution = {}
    for i in range(bins):
        bin_start = min_price + i * bin_width
        bin_end = min_price + (i + 1) * bin_width
        key = f"{bin_start:.2f}-{bin_end:.2f}"
        count = sum(1 for p in prices if bin_start <= p < bin_end or (i == bins - 1 and p == max_price))
        distribution[key] = count
    
    for key, value in distribution.items():
        print(f"{key}={value}")
"""
    GlobalScriptExecutor.executeCode(Python, pythonCode).map { output =>
      if (output.contains("EMPTY")) {
        Map.empty[String, Int]
      } else {
        output.split("\n").filter(_.contains("=")).map { line =>
          val parts = line.split("=")
          parts(0) -> parts(1).toInt
        }.toMap
      }
    }
  }
  
  private def parsePythonStats(output: String): Map[String, Any] = {
    val lines = output.split("\n")
    lines.filter(_.contains("=")).map { line =>
      val parts = line.split("=", 2)
      if (parts.length == 2) {
        val key = parts(0).toLowerCase
        val value = try {
          if (parts(1).contains(".")) parts(1).toDouble else parts(1).toInt
        } catch {
          case _: NumberFormatException => parts(1)
        }
        key -> value
      } else {
        "" -> ""
      }
    }.filter(_._1.nonEmpty).toMap
  }
}

