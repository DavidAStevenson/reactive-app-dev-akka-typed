package com.rarebooks.library

import akka.actor.typed.{ Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }

object Librarian {
  def apply(): Behavior[RareBooksProtocol.Msg] = Behaviors.setup { context =>
      new Librarian(context).ready()
  }
} 

class Librarian(context: ActorContext[RareBooksProtocol.Msg]) {

  context.log.info("Librarian started")

  protected def ready(): Behavior[RareBooksProtocol.Msg] =
    Behaviors.receiveMessage {
      case _ =>
        context.log.info("Librarian ignoring messages")
      Behaviors.same
    }
  
}
