package com.rarebooks.library

import java.lang.System.currentTimeMillis
import akka.actor.typed.{ ActorRef }

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
      pages: Int
  ) extends Card

  /* trait for all messages. */
  trait BaseMsg

  /* trait for all public messages. */
  trait Msg extends BaseMsg {
    def dateInMillis: Long
  }

  /* List of book cards found message.
   *
   * @param books  list of book cards
   * @param dateInMillis date message was created
   */
  final case class BookFound(books: List[BookCard], dateInMillis: Long = currentTimeMillis)
      extends Msg {
    require(books.nonEmpty, "Book(s) required.")
  }

  /**
    * Book was not found message.
    *
    * @param reason reason book was not found
    * @param dateInMillis date message was created
    */
  final case class BookNotFound(reason: String, dateInMillis: Long = currentTimeMillis)
      extends Msg {
    require(reason.nonEmpty, "Reason is required.")
  }

  /**
    * Complain message when book not found.
    *
    * @param dateInMillis date message was created
    */
  final case class Complain(replyTo: ActorRef[Msg], dateInMillis: Long = currentTimeMillis)
      extends Msg

  /**
    * Credit message
    *
    * @param dateInMillis date message was created
    */
  final case class Credit(dateInMillis: Long = currentTimeMillis) extends Msg

  /**
    * Find book by author message.
    *
    * @param author author to search for
    * @param dateInMillis date message was created
    */
  final case class FindBookByAuthor(
      author: String,
      replyTo: ActorRef[Msg],
      dateInMillis: Long = currentTimeMillis
  ) extends Msg {
    require(author.nonEmpty, "Author required.")
  }

  /**
    * Find book by isbn message.
    *
    * @param isbn isbn to search for
    * @param dateInMillis date message was created
    */
  final case class FindBookByIsbn(
      isbn: String,
      replyTo: ActorRef[Msg],
      dateInMillis: Long = currentTimeMillis
  ) extends Msg {
    require(isbn.nonEmpty, "Isbn required.")
  }

  /**
    * Find book by topic.
    *
    * @param topic set of topics to search for
    * @param dateInMillis date message was created
    */
  final case class FindBookByTopic(
      topic: Set[Topic],
      replyTo: ActorRef[Msg],
      dateInMillis: Long = currentTimeMillis
  ) extends Msg {
    require(topic.nonEmpty, "Topic required.")
  }

  /**
    * Find book by title message.
    *
    * @param title title to search for
    * @param dateInMillis date message was created
    */
  final case class FindBookByTitle(
      title: String,
      replyTo: ActorRef[Msg],
      dateInMillis: Long = currentTimeMillis
  ) extends Msg {
    require(title.nonEmpty, "Title required.")
  }

}
