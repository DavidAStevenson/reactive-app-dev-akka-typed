package com.rarebooks.library

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }

object Customer {
  def apply(): Behavior[RareBooksProtocol.Msg] =
    Behaviors.setup { context =>
      new Customer(context).receive
    }

  case class CustomerModel(found: Int)
}

class Customer(context: ActorContext[RareBooksProtocol.Msg]) {

  context.log.info("Customer started")

  protected def receive(): Behavior[RareBooksProtocol.Msg] =
    Behaviors.receiveMessage {
      case b: RareBooksProtocol.BookFound =>
        context.log.info(f"${b.books.size}%d Book(s) found!")
        Behaviors.same
      case m: RareBooksProtocol.Msg =>
        context.log.info(s"Received a message: ${m}")
        Behaviors.same
    }
}
