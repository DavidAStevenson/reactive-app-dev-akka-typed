package com.rarebooks.library

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }

object Librarian {

  import RareBooksProtocol._

  def optToEither[T](value: T, func: T => Option[List[BookCard]]):
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
        val result = optToEither(topic, Catalog.findBookByTopic)
        process(result, replyTo)
        Behaviors.same
      case FindBookByTitle(title, replyTo, _) =>
        val result = optToEither(title, Catalog.findBookByTitle)
        process(result, replyTo)
        Behaviors.same
      case FindBookByAuthor(author, replyTo, _) =>
        val result = optToEither(author, Catalog.findBookByAuthor)
        process(result, replyTo)
        Behaviors.same
      case FindBookByIsbn(isbn, replyTo, _) =>
        val result = optToEither(isbn, Catalog.findBookByIsbn)
        process(result, replyTo)
        Behaviors.same
    }

  private def process(result: Either[BookNotFound, BookFound], replyTo: ActorRef[Msg]): Unit = {
    result.fold (
      bookNotFound => replyTo ! bookNotFound,
      bookFound    => replyTo ! bookFound
    )
  }

}
