package com.rarebooks.library

import scala.concurrent.duration.FiniteDuration
import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors, StashBuffer, TimerScheduler }

import RareBooksProtocol._

object Librarian {

  sealed trait PrivateCommand extends BaseMsg
  private[library] case class GetState(replyTo: ActorRef[PrivateResponse]) extends PrivateCommand
  case class NotifyResearchResult(
      result: Msg,
      replyTo: ActorRef[Msg]
  ) extends PrivateCommand

  sealed trait PrivateResponse extends BaseMsg
  private[library] case object Busy extends PrivateResponse
  private[library] case object Ready extends PrivateResponse
  private[library] case class Busy(stashSize: Int) extends PrivateResponse

  private case object TimerKey

  def apply(findBookDuration: FiniteDuration): Behavior[Msg] =
    Behaviors.setup { context =>
      setup(findBookDuration).narrow
    }

  private[library] def setup(
      findBookDuration: FiniteDuration
  ): Behavior[BaseMsg] =
    Behaviors.setup[BaseMsg] { context =>
      Behaviors.withTimers { timers =>
        val stashSize = context.system.settings.config.getInt("rare-books.librarian.stash-size")
        Behaviors.withStash(stashSize) { buffer =>
          new Librarian(context, timers, buffer, findBookDuration).ready()
        }
      }
    }

}

class Librarian(
    context: ActorContext[BaseMsg],
    timers: TimerScheduler[BaseMsg],
    buffer: StashBuffer[BaseMsg],
    findBookDuration: FiniteDuration
) {

  context.log.info("Librarian started")

  import Librarian._

  protected def ready(): Behavior[BaseMsg] =
    Behaviors.receiveMessage {
      case Complain(replyTo, _) =>
        context.log.info(s"Credit issued to customer ${replyTo}")
        replyTo ! Credit()
        Behaviors.same
      case m: Msg =>
        research(m)
        busy()
      case GetState(replyTo) =>
        replyTo ! Ready
        Behaviors.same
    }

  protected def busy(): Behavior[BaseMsg] =
    Behaviors.receiveMessage {
      case NotifyResearchResult(result, replyTo) =>
        replyTo ! result
        buffer.unstashAll(ready())
      case GetState(replyTo) =>
        if (buffer.isEmpty) replyTo ! Busy
        else replyTo ! Busy(buffer.size)
        Behaviors.same
      case other =>
        if (buffer.isFull)
          context.log.warn("stash full while busy, dropping new incoming message")
        else
          buffer.stash(other)
        Behaviors.same
    }

  private def optToEither[T](
      value: T,
      func: T => Option[List[BookCard]]
  ): Either[BookNotFound, BookFound] =
    func(value) match {
      case Some(b) => Right(BookFound(b))
      case None    => Left(BookNotFound(s"No book(s) matching ${value}.", context.self))
    }

  private def research(request: Msg): Unit = {
    request match {
      case FindBookByTopic(topic, replyTo, _) =>
        process(optToEither(topic, Catalog.findBookByTopic), replyTo)
      case FindBookByTitle(title, replyTo, _) =>
        process(optToEither(title, Catalog.findBookByTitle), replyTo)
      case FindBookByAuthor(author, replyTo, _) =>
        process(optToEither(author, Catalog.findBookByAuthor), replyTo)
      case FindBookByIsbn(isbn, replyTo, _) =>
        process(optToEither(isbn, Catalog.findBookByIsbn), replyTo)
      case msg =>
        context.log.info(s"Unknown research request: ${msg}")
    }
  }

  private def notifyResultLater(msg: Msg, replyTo: ActorRef[Msg]): Unit =
    timers.startSingleTimer(TimerKey, NotifyResearchResult(msg, replyTo), findBookDuration)

  private def process(result: Either[BookNotFound, BookFound], replyTo: ActorRef[Msg]): Unit = {
    result.fold(
      bookNotFound => notifyResultLater(bookNotFound, replyTo),
      bookFound => notifyResultLater(bookFound, replyTo)
    )
  }

}
