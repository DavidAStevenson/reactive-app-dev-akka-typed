package com.rarebooks.library

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }

object Librarian {

  import RareBooksProtocol._

  sealed trait PrivateCommand extends RareBooksProtocol.BaseMsg
  private[library] case class GetState(replyTo: ActorRef[PrivateResponse]) extends PrivateCommand

  sealed trait PrivateResponse extends RareBooksProtocol.BaseMsg
  private[library] case object Busy extends PrivateResponse
  private[library] case object Ready extends PrivateResponse

  def optToEither[T](value: T, func: T => Option[List[BookCard]]):
    Either[BookNotFound, BookFound] =
      func(value) match {
        case Some(b) => Right(BookFound(b))
        case None    => Left(BookNotFound(s"No book(s) matching ${value}."))
      }

  def apply(): Behavior[RareBooksProtocol.Msg] = Behaviors.setup { context =>
    setup().
    narrow
  }

  private[library] def setup(): Behavior[RareBooksProtocol.BaseMsg] =
    Behaviors.setup[RareBooksProtocol.BaseMsg] { context =>
      new Librarian(context).ready()
    }

} 

class Librarian(context: ActorContext[RareBooksProtocol.BaseMsg]) {

  context.log.info("Librarian started")

  import Librarian._
  import RareBooksProtocol._

  protected def ready(): Behavior[RareBooksProtocol.BaseMsg] =
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
