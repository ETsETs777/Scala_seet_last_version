package com.example.util

/**
 * Утилиты для работы с коллекциями
 */
object CollectionUtils {
  
  // Группировка с преобразованием
  def groupByAndTransform[A, K, V](
    list: List[A],
    keyFunc: A => K,
    valueFunc: A => V
  ): Map[K, List[V]] = {
    list.groupBy(keyFunc).view.mapValues(_.map(valueFunc)).toMap
  }
  
  // Разбиение на части определенного размера
  def chunk[A](list: List[A], size: Int): List[List[A]] = {
    if (size <= 0) List.empty
    else list.grouped(size).toList
  }
  
  // Удаление дубликатов с сохранением порядка
  def distinctBy[A, K](list: List[A])(f: A => K): List[A] = {
    var seen = Set.empty[K]
    list.filter { elem =>
      val key = f(elem)
      if (seen.contains(key)) {
        false
      } else {
        seen += key
        true
      }
    }
  }
  
  // Подсчет вхождений
  def frequency[A](list: List[A]): Map[A, Int] = {
    list.foldLeft(Map.empty[A, Int].withDefaultValue(0)) { (acc, elem) =>
      acc + (elem -> (acc(elem) + 1))
    }
  }
  
  // Найти все вхождения элемента
  def findAllIndices[A](list: List[A], predicate: A => Boolean): List[Int] = {
    list.zipWithIndex.filter { case (elem, _) => predicate(elem) }.map(_._2)
  }
  
  // Разделить список на два по условию
  def partitionBy[A](list: List[A], predicate: A => Boolean): (List[A], List[A]) = {
    list.partition(predicate)
  }
  
  // Применить функцию к каждому элементу с индексом
  def mapWithIndex[A, B](list: List[A])(f: (A, Int) => B): List[B] = {
    list.zipWithIndex.map { case (elem, idx) => f(elem, idx) }
  }
  
  // Фильтровать и трансформировать одновременно
  def collect[A, B](list: List[A])(pf: PartialFunction[A, B]): List[B] = {
    list.collect(pf)
  }
  
  // Скользящее окно
  def slidingPairs[A](list: List[A]): List[(A, A)] = {
    if (list.length < 2) List.empty
    else list.sliding(2).map { case List(a, b) => (a, b) }.toList
  }
  
  // Разделить список на несколько по размеру
  def splitInto[A](list: List[A], parts: Int): List[List[A]] = {
    if (parts <= 0) List.empty
    else {
      val size = math.ceil(list.length.toDouble / parts).toInt
      chunk(list, size)
    }
  }
  
  // Объединить несколько списков в один
  def flattenOption[A](list: List[Option[A]]): List[A] = {
    list.collect { case Some(a) => a }
  }
  
  // Найти максимум с использованием функции
  def maxByOption[A, B: Ordering](list: List[A])(f: A => B): Option[A] = {
    if (list.isEmpty) None
    else Some(list.maxBy(f))
  }
  
  // Найти минимум с использованием функции
  def minByOption[A, B: Ordering](list: List[A])(f: A => B): Option[A] = {
    if (list.isEmpty) None
    else Some(list.minBy(f))
  }
  
  // Сортировка по нескольким критериям
  def sortByMultiple[A](list: List[A])(orderings: (A => Comparable[_])*): List[A] = {
    implicit val multiOrdering: Ordering[A] = new Ordering[A] {
      def compare(x: A, y: A): Int = {
        orderings.foldLeft(0) { (acc, ord) =>
          if (acc != 0) acc
          else ord(x).asInstanceOf[Comparable[Any]].compareTo(ord(y).asInstanceOf[Comparable[Any]])
        }
      }
    }
    list.sorted
  }
  
  // Преобразовать Map в список пар, отсортированных по значению
  def mapToSortedPairs[A, B: Ordering](map: Map[A, B]): List[(A, B)] = {
    map.toList.sortBy(_._2)
  }
}

