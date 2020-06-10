package com.rarebooks.library

import scala.concurrent.duration.{ MILLISECONDS => Millis, FiniteDuration }
import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors, TimerScheduler }

object RareBooks {

  sealed trait Command
  private[library] case object Open extends Command
  private[library] case object Close extends Command
  private[library] case object Report extends Command

  private case object TimerKey

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    context.log.info("RareBooks started")
    Behaviors.withTimers { timers => new RareBooks(context, timers).open() }
  }

}

class RareBooks(context: ActorContext[RareBooks.Command], timers: TimerScheduler[RareBooks.Command]) {
  import RareBooks._

  private def open(): Behavior[Command] =
    Behaviors.receiveMessage {
      case Open =>
        context.log.info("Time to open up!")
        timers.startSingleTimer(TimerKey, Close, FiniteDuration(10000, Millis))
        Behaviors.same
      case Close =>
        context.log.info("Time to close!")
        timers.startSingleTimer(TimerKey, Open, FiniteDuration(10000, Millis))
        Behaviors.same
      case Report =>
        context.log.info("Time to produce a report.")
        Behaviors.same
    }
}
