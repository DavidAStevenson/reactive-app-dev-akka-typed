package com.rarebooks.library

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }

object Customer {

  case class CustomerModel(found: Int)

  sealed trait PrivateCommand extends RareBooksProtocol.BaseMsg
  private[library] final case class GetCustomer(replyTo: ActorRef[CustomerModel]) extends PrivateCommand

  def apply(): Behavior[RareBooksProtocol.Msg] =
    setup().narrow

  private[library] def testApply(): Behavior[RareBooksProtocol.BaseMsg] =
    setup()

  private[library] def setup(): Behavior[RareBooksProtocol.BaseMsg] =
    Behaviors.setup[RareBooksProtocol.BaseMsg] { context =>
      new Customer(context).receive()
    }

}

class Customer(context: ActorContext[RareBooksProtocol.BaseMsg]) {

  context.log.info("Customer started")

  import Customer._

  protected def receive(): Behavior[RareBooksProtocol.BaseMsg] =
    Behaviors.receiveMessage {
      case b: RareBooksProtocol.BookFound =>
        context.log.info(f"${b.books.size}%d Book(s) found!")
        Behaviors.same
      case GetCustomer(replyTo) =>
        replyTo ! CustomerModel(1)
        Behaviors.same
      case m: RareBooksProtocol.Msg =>
        context.log.info(s"Received a message: ${m}")
        Behaviors.same
    }
}
