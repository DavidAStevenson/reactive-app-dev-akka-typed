package com.rarebooks.library

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }

object RareBooks {

  sealed trait Command
  private case object Open extends Command
  private case object Close extends Command
  private case object Report extends Command

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    context.log.info("RareBooks started")
    Behaviors.empty
  }

}
