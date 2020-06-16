package com.rarebooks.library

import scala.concurrent.duration.{ MILLISECONDS => Millis, FiniteDuration }
import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors, TimerScheduler }

object RareBooks {

  sealed trait PrivateCommand extends RareBooksProtocol.BaseMsg
  private[library] case object Open extends PrivateCommand
  private[library] case object Close extends PrivateCommand
  private[library] case object Report extends PrivateCommand
  private[library] case class ChangeLibrarian(librarian: ActorRef[RareBooksProtocol.Msg]) extends PrivateCommand

  private case object TimerKey

  def apply(name: String): Behavior[RareBooksProtocol.Msg] =
    setup(name)
    .narrow

  private[library] def setup(name: String): Behavior[RareBooksProtocol.BaseMsg] =
    Behaviors.setup[RareBooksProtocol.BaseMsg] { context =>
      Behaviors.withTimers {
        timers => new RareBooks(context, timers, name).open()
      }
    }

}
class RareBooks(
  context: ActorContext[RareBooksProtocol.BaseMsg],
  timers: TimerScheduler[RareBooksProtocol.BaseMsg],
  bookStoreName: String) {
  import RareBooks._
  import RareBooksProtocol._

  private var librarian = createLibrarian()

  private def init(): Unit = {
    logInfo("RareBooks started")
    timers.startSingleTimer(TimerKey, Close, FiniteDuration(10000, Millis))
  }

  init()

  private def logInfo(message: String): Unit =
    context.log.info(s"${bookStoreName}: ${message}")

  private def open(): Behavior[BaseMsg] =
    Behaviors.receiveMessage {
      case msg: Msg =>
        logInfo("Received a Msg. Forwarding it to librarian")
        librarian ! msg
        Behaviors.same
      case Open =>
        logInfo("We're already open.")
        Behaviors.same
      case Close =>
        logInfo("Time to close!")
        timers.startSingleTimer(TimerKey, Open, FiniteDuration(10000, Millis))
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
        timers.startSingleTimer(TimerKey, Close, FiniteDuration(10000, Millis))
        open()
      case Close =>
        logInfo("We're already closed.")
        Behaviors.same
      case Report =>
        logInfo("Time to produce a report.")
        Behaviors.same
      case ChangeLibrarian(ref) =>
        changeLibrarian(ref)
        Behaviors.same
    }

  private def createLibrarian(): ActorRef[Msg] = {
    context.spawn(Librarian(), "librarian")
  }

  private def changeLibrarian(ref: ActorRef[Msg]) = {
    context.stop(librarian)
    librarian = ref
  }

}
