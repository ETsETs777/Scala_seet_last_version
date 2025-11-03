package com.example.util


case class PageResult[T](
  items: List[T],
  page: Int,
  pageSize: Int,
  totalItems: Int,
  totalPages: Int
) {
  def hasNext: Boolean = page < totalPages
  def hasPrevious: Boolean = page > 1
  def startIndex: Int = (page - 1) * pageSize + 1
  def endIndex: Int = math.min(page * pageSize, totalItems)
}


object Pagination {
  
  
  def paginate[T](items: List[T], page: Int, pageSize: Int): PageResult[T] = {
    val totalItems = items.size
    val totalPages = math.ceil(totalItems.toDouble / pageSize).toInt
    val validPage = math.max(1, math.min(page, totalPages))
    val startIndex = (validPage - 1) * pageSize
    val endIndex = math.min(startIndex + pageSize, totalItems)
    val pageItems = items.slice(startIndex, endIndex)
    
    PageResult(
      items = pageItems,
      page = validPage,
      pageSize = pageSize,
      totalItems = totalItems,
      totalPages = if (totalPages == 0) 1 else totalPages
    )
  }
  
  
  def emptyPage[T](page: Int = 1, pageSize: Int = 10): PageResult[T] = {
    PageResult(
      items = List.empty,
      page = page,
      pageSize = pageSize,
      totalItems = 0,
      totalPages = 1
    )
  }
  
  
  def validate(page: Int, pageSize: Int): Option[(Int, Int)] = {
    if (page < 1 || pageSize < 1 || pageSize > 1000) {
      None
    } else {
      Some((page, math.min(pageSize, 1000)))
    }
  }
}




