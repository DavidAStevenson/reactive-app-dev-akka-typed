package com.rarebooks.library

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }

object RareBooks {

  sealed trait Command
  private[library] case object Open extends Command
  private[library] case object Close extends Command
  private[library] case object Report extends Command

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    context.log.info("RareBooks started")
    new RareBooks(context).open()
  }

}

class RareBooks(context: ActorContext[RareBooks.Command]) {
  import RareBooks._

  private def open(): Behavior[Command] =
    Behaviors.receiveMessage {
      case Open =>
        context.log.info("Time to open up!")
        Behaviors.same
      case Close =>
        context.log.info("Time to close!")
        Behaviors.same
      case Report =>
        context.log.info("Time to produce a report.")
        Behaviors.same
    }
}
