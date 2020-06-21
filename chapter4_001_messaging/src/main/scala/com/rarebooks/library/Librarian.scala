package com.rarebooks.library

import scala.concurrent.duration.FiniteDuration
import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors, TimerScheduler }

object Librarian {

  import RareBooksProtocol._

  sealed trait PrivateCommand extends RareBooksProtocol.BaseMsg
  private[library] case class GetState(replyTo: ActorRef[PrivateResponse]) extends PrivateCommand
  case class NotifyResearchResult(result: RareBooksProtocol.Msg, replyTo: ActorRef[RareBooksProtocol.Msg]) extends PrivateCommand

  sealed trait PrivateResponse extends RareBooksProtocol.BaseMsg
  private[library] case object Busy extends PrivateResponse
  private[library] case object Ready extends PrivateResponse

  private case object TimerKey

  def optToEither[T](value: T, func: T => Option[List[BookCard]]):
    Either[BookNotFound, BookFound] =
      func(value) match {
        case Some(b) => Right(BookFound(b))
        case None    => Left(BookNotFound(s"No book(s) matching ${value}."))
      }

  def apply(findBookDuration: FiniteDuration): Behavior[RareBooksProtocol.Msg] = Behaviors.setup { context =>
    setup(findBookDuration).
    narrow
  }

  private[library] def setup(findBookDuration: FiniteDuration): Behavior[RareBooksProtocol.BaseMsg] =
    Behaviors.withTimers { timers =>
      Behaviors.setup[RareBooksProtocol.BaseMsg] { context =>
        new Librarian(context, timers, findBookDuration).ready()
      }
    }

} 

class Librarian(
  context: ActorContext[RareBooksProtocol.BaseMsg],
  timers: TimerScheduler[RareBooksProtocol.BaseMsg],
  findBookDuration: FiniteDuration) {

  context.log.info("Librarian started")

  import Librarian._
  import RareBooksProtocol._

  protected def ready(): Behavior[RareBooksProtocol.BaseMsg] =
    Behaviors.receiveMessage {
      case FindBookByTopic(topic, replyTo, _) =>
        val result = optToEither(topic, Catalog.findBookByTopic)
        process(result, replyTo)
        busy()
      case FindBookByTitle(title, replyTo, _) =>
        val result = optToEither(title, Catalog.findBookByTitle)
        process(result, replyTo)
        busy()
      case FindBookByAuthor(author, replyTo, _) =>
        val result = optToEither(author, Catalog.findBookByAuthor)
        process(result, replyTo)
        busy()
      case FindBookByIsbn(isbn, replyTo, _) =>
        val result = optToEither(isbn, Catalog.findBookByIsbn)
        process(result, replyTo)
        busy()
      case GetState(replyTo) =>
        replyTo ! Ready
        Behaviors.same
    }

  protected def busy(): Behavior[RareBooksProtocol.BaseMsg] =
    Behaviors.receiveMessage {
      case NotifyResearchResult(result, replyTo) =>
        replyTo ! result
        ready()
      case GetState(replyTo) =>
        replyTo ! Busy
        Behaviors.same
      case other =>
        Behaviors.same
    }

  private def notifyResultLater(msg: Msg, replyTo: ActorRef[Msg]): Unit =
    timers.startSingleTimer(TimerKey, NotifyResearchResult(msg, replyTo), findBookDuration)

  private def process(result: Either[BookNotFound, BookFound], replyTo: ActorRef[Msg]): Unit = {
    result.fold (
      bookNotFound => notifyResultLater(bookNotFound, replyTo),
      bookFound    => notifyResultLater(bookFound, replyTo)
    )
  }

}
