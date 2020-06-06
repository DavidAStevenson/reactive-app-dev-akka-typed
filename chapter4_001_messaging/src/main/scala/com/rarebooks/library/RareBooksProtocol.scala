package com.rarebooks.library

import java.lang.System.currentTimeMillis

/*
 * The shared data structures and messages for the rare books info service.
 */
object RareBooksProtocol {

  sealed trait Topic
  case object Africa extends Topic
  case object Asia extends Topic
  case object Gilgamesh extends Topic
  case object Greece extends Topic
  case object Persia extends Topic
  case object Philosophy extends Topic
  case object Royalty extends Topic
  case object Tradition extends Topic
  case object Unknown extends Topic

  /**
  * Card trait for book cards.
  */
  sealed trait Card {
    def title: String
    def description: String
    def topic: Set[Topic]
  }

  /**
   * Book card class.
   *
   * @param isbn the book isbn
   * @param author the book author
   * @param title the book title
   * @param description the book description
   * @param dateOfOrigin the book date of origin
   * @param topic set of associated tags for the book
   * @param publisher the book publisher
   * @param language the language the book is in
   * @param pages the number of pages in the book
   */
  final case class BookCard(
      isbn: String,
      author: String,
      title: String,
      description: String,
      dateOfOrigin: String,
      topic: Set[Topic],
      publisher: String,
      language: String,
      pages: Int)
    extends Card

  /* trait for all messages. */
  trait Msg {
    def dateInMillis: Long
  }

  /* List of book cards found message.
   *
   * @param books  list of book cards
   * @param dateInMillis date message was created
   */
  final case class BookFound(books: List[BookCard], dateInMillis: Long = currentTimeMillis) extends Msg {
    require(books.nonEmpty, "Book(s) required.")
  }
}
