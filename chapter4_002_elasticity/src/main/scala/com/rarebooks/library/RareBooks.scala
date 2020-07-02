package com.rarebooks.library

import scala.concurrent.duration.{ MILLISECONDS => Millis, FiniteDuration, Duration, SECONDS }
import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors, StashBuffer, TimerScheduler }

import RareBooksProtocol._

object RareBooks {

  sealed trait PrivateCommand extends BaseMsg
  private[library] case object Open extends PrivateCommand
  private[library] case object Close extends PrivateCommand
  private[library] case object Report extends PrivateCommand
  private[library] case class ChangeLibrarian(librarian: ActorRef[Msg]) extends PrivateCommand

  private case object TimerKey

  def apply(name: String): Behavior[Msg] =
    setup(name).narrow

  private[library] def setup(name: String): Behavior[BaseMsg] =
    Behaviors.setup[BaseMsg] { context =>
      val stashSize = context.system.settings.config.getInt("rare-books.stash-size")
      Behaviors.withStash(stashSize) { buffer =>
        Behaviors.withTimers { timers =>
          new RareBooks(context, timers, buffer, name).open()
        }
      }
    }

}
class RareBooks(
    context: ActorContext[BaseMsg],
    timers: TimerScheduler[BaseMsg],
    buffer: StashBuffer[BaseMsg],
    bookStoreName: String
) {
  import RareBooks._

  private val openDuration: FiniteDuration =
    Duration(context.system.settings.config.getDuration("rare-books.open-duration", Millis), Millis)

  private val closeDuration: FiniteDuration =
    Duration(
      context.system.settings.config.getDuration("rare-books.close-duration", Millis),
      Millis
    )

  private val findBookDuration: FiniteDuration =
    Duration(
      context.system.settings.config.getDuration("rare-books.librarian.find-book-duration", Millis),
      Millis
    )

  private val nbrOfLibrarians: Int = context.system.settings.config.getInt("rare-books.nbr-of-librarians")

  private var librarian = createLibrarian(findBookDuration)
  private var requestsToday: Int = 0
  private var totalRequests: Int = 0

  private def init(): Unit = {
    logInfo("RareBooks started")
    timers.startSingleTimer(TimerKey, Close, openDuration)
  }

  init()

  private def logInfo(message: String): Unit =
    context.log.info(s"${bookStoreName}: ${message}")

  private def open(): Behavior[BaseMsg] =
    Behaviors.receiveMessage {
      case msg: Msg =>
        logInfo("Received a Msg. Forwarding it to librarian")
        librarian ! msg
        requestsToday += 1
        Behaviors.same
      case Open =>
        logInfo("We're already open.")
        Behaviors.same
      case Close =>
        logInfo("Time to close!")
        timers.startSingleTimer(TimerKey, Open, closeDuration)
        context.self ! Report
        closed()
      case Report =>
        logInfo("We only produce reports while closed")
        Behaviors.same
      case ChangeLibrarian(ref) =>
        changeLibrarian(ref)
        Behaviors.same
    }

  private def closed(): Behavior[BaseMsg] =
    Behaviors.receiveMessage {
      case Open =>
        logInfo("Time to open up!")
        timers.startSingleTimer(TimerKey, Close, openDuration)
        buffer.unstashAll(open())
      case Close =>
        logInfo("We're already closed.")
        Behaviors.same
      case Report =>
        totalRequests += requestsToday
        logInfo(
          s"${requestsToday} requests processed today. Total requests processed = ${totalRequests}"
        )
        requestsToday = 0
        Behaviors.same
      case ChangeLibrarian(ref) =>
        changeLibrarian(ref)
        Behaviors.same
      case other =>
        if (buffer.isFull)
          context.log.warn("stash full while Closed, dropping new incoming message")
        else
          buffer.stash(other)
        Behaviors.same
    }

  private def createLibrarian(findBookDuration: FiniteDuration): ActorRef[Msg] = {
    context.spawn(Librarian(findBookDuration), "librarian")
  }

  private def changeLibrarian(ref: ActorRef[Msg]) = {
    context.stop(librarian)
    librarian = ref
  }

}
