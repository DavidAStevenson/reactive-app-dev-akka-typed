package com.rarebooks.library

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }

object RareBooks {

  sealed trait Command
  private[library] case object Open extends Command
  private case object Close extends Command
  private case object Report extends Command

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
      case _ =>
        Behaviors.same
    }
}
