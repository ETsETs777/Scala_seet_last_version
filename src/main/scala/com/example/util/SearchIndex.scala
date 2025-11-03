package com.example.util

import scala.collection.mutable

class SearchIndex[T] {
  private val index = mutable.Map[String, mutable.Set[T]]()
  
  def indexItem(key: String, item: T): Unit = {
    val normalizedKey = normalize(key)
    index.getOrElseUpdate(normalizedKey, mutable.Set.empty) += item
  }
  
  def indexItems(keywords: List[String], item: T): Unit = {
    keywords.foreach(keyword => indexItem(keyword, item))
  }
  
  def search(query: String): Set[T] = {
    val normalizedQuery = normalize(query)
    val queryParts = normalizedQuery.split("\\s+")
    
    queryParts.foldLeft(Set.empty[T]) { (acc, part) =>
      index.get(part).map(_.toSet).getOrElse(Set.empty) ++ acc
    }
  }
  
  def removeItem(key: String, item: T): Unit = {
    val normalizedKey = normalize(key)
    index.get(normalizedKey).foreach(_.remove(item))
  }
  
  def clear(): Unit = {
    index.clear()
  }
  
  def getIndexSize: Int = {
    index.size
  }
  
  private def normalize(str: String): String = {
    str.toLowerCase.trim
  }
  
  def rebuild(items: Map[String, T]): Unit = {
    clear()
    items.foreach { case (key, item) =>
      indexItem(key, item)
    }
  }
}


