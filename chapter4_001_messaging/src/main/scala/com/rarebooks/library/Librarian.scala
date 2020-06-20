package com.rarebooks.library

import akka.actor.typed.{ Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }

object Librarian {

  import RareBooksProtocol._

  def optToEither[String](value: String, func: String => Option[List[BookCard]]):
    Either[BookNotFound, BookFound] =
      func(value) match {
        case Some(b) => Right(BookFound(b))
        case None    => Left(BookNotFound(s"No book(s) matching ${value}."))
      }

  def apply(): Behavior[RareBooksProtocol.Msg] = Behaviors.setup { context =>
      new Librarian(context).ready()
  }
} 

class Librarian(context: ActorContext[RareBooksProtocol.Msg]) {

  context.log.info("Librarian started")

  import Librarian._
  import RareBooksProtocol._

  protected def ready(): Behavior[RareBooksProtocol.Msg] =
    Behaviors.receiveMessage {
      case FindBookByTopic(topic, replyTo, _) =>
        val book = Catalog.findBookByTopic(topic)
        book match {
          case Some(b) => replyTo ! BookFound(book.get)
          case None => replyTo ! BookNotFound(s"No book(s) matching ${topic}.")
        }
        Behaviors.same
      case FindBookByTitle(title, replyTo, _) =>
        val result = optToEither(title, Catalog.findBookByTitle)
        result.fold (
          fa => replyTo ! fa,
          fb => replyTo ! fb
        )
        Behaviors.same
      case FindBookByAuthor(author, replyTo, _) =>
        val result = optToEither(author, Catalog.findBookByAuthor)
        result.fold (
          fa => replyTo ! fa,
          fb => replyTo ! fb
        )
        Behaviors.same
      case FindBookByIsbn(isbn, replyTo, _) =>
        val result = optToEither(isbn, Catalog.findBookByIsbn)
        result.fold (
          fa => replyTo ! fa,
          fb => replyTo ! fb
        )
        Behaviors.same
    }
  
}
