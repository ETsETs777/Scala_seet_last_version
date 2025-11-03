package com.example.repository


trait Repository[T, ID] {
  
  def findById(id: ID): Option[T]
  
  
  def save(entity: T): T
  
  
  def delete(id: ID): Boolean
  
  
  def findAll: List[T]
  
  
  def exists(id: ID): Boolean = findById(id).isDefined
}


class InMemoryRepository[T, ID](idExtractor: T => ID) extends Repository[T, ID] {
  private var cache: Map[ID, T] = Map.empty
  
  override def findById(id: ID): Option[T] = {
    cache.get(id)
  }
  
  override def save(entity: T): T = {
    val id = idExtractor(entity)
    cache = cache + (id -> entity)
    entity
  }
  
  override def delete(id: ID): Boolean = {
    if (cache.contains(id)) {
      cache = cache - id
      true
    } else {
      false
    }
  }
  
  override def findAll: List[T] = {
    cache.values.toList
  }
  
  
  def clear(): Unit = {
    cache = Map.empty
  }
  
  
  def count: Int = cache.size
}




