package com.rarebooks.library

import scala.concurrent.duration.{ MILLISECONDS => Millis, FiniteDuration }
import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors, TimerScheduler }

object RareBooks {

  sealed trait PrivateCommand extends RareBooksProtocol.BaseMsg
  private[library] case object Open extends PrivateCommand
  private[library] case object Close extends PrivateCommand
  private[library] case object Report extends PrivateCommand

  private case object TimerKey

  def apply(): Behavior[RareBooksProtocol.Msg] =
    setup()
    .narrow

  private[library] def setup(): Behavior[RareBooksProtocol.BaseMsg] =
    Behaviors.setup[RareBooksProtocol.BaseMsg] { context =>
      Behaviors.withTimers {
        timers => new RareBooks(context, timers).closed()
      }
    }

}
class RareBooks(context: ActorContext[RareBooksProtocol.BaseMsg], timers: TimerScheduler[RareBooksProtocol.BaseMsg]) {
  import RareBooks._

  private val librarian = createLibrarian()

  private def init(): Unit = {
    context.log.info("RareBooks started")
  }

  init()

  private def open(): Behavior[RareBooksProtocol.BaseMsg] =
    Behaviors.receiveMessage {
      case Open =>
        context.log.info("We're already open.")
        Behaviors.same
      case Close =>
        context.log.info("Time to close!")
        timers.startSingleTimer(TimerKey, Open, FiniteDuration(10000, Millis))
        context.self ! Report
        closed()
      case Report =>
        context.log.info("We only produce reports while closed")
        Behaviors.same
    }

  private def closed(): Behavior[RareBooksProtocol.BaseMsg] =
    Behaviors.receiveMessage {
      case Open =>
        context.log.info("Time to open up!")
        timers.startSingleTimer(TimerKey, Close, FiniteDuration(10000, Millis))
        open()
      case Close =>
        context.log.info("We're already closed.")
        Behaviors.same
      case Report =>
        context.log.info("Time to produce a report.")
        Behaviors.same
    }

  private def createLibrarian(): ActorRef[RareBooksProtocol.Msg] = {
    context.spawn(Librarian(), "librarian")
  }

}
