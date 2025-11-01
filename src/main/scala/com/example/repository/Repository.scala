package com.example.repository

/**
 * Базовый интерфейс для репозиториев
 * 
 * Предоставляет абстракцию для работы с хранилищем данных
 */
trait Repository[T, ID] {
  /**
   * Находит сущность по идентификатору
   * 
   * @param id идентификатор сущности
   * @return опциональная сущность
   */
  def findById(id: ID): Option[T]
  
  /**
   * Сохраняет сущность
   * 
   * @param entity сущность для сохранения
   * @return сохраненная сущность
   */
  def save(entity: T): T
  
  /**
   * Удаляет сущность по идентификатору
   * 
   * @param id идентификатор сущности
   * @return true если удаление успешно, false иначе
   */
  def delete(id: ID): Boolean
  
  /**
   * Находит все сущности
   * 
   * @return список всех сущностей
   */
  def findAll: List[T]
  
  /**
   * Проверяет существование сущности по идентификатору
   * 
   * @param id идентификатор сущности
   * @return true если сущность существует
   */
  def exists(id: ID): Boolean = findById(id).isDefined
}

/**
 * In-memory репозиторий с кэшированием
 */
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
  
  /**
   * Очищает кэш
   */
  def clear(): Unit = {
    cache = Map.empty
  }
  
  /**
   * Получает количество элементов в репозитории
   */
  def count: Int = cache.size
}

