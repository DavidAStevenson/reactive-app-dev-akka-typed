package com.rarebooks.library

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }

object Customer {

  import RareBooksProtocol._

  case class CustomerModel(found: Int)
  private case class State(model: CustomerModel, timeInMillis: Long) {
    def update(m: Msg): State =
      m match {
        case BookFound(b, d) =>
          //import java.lang.System.currentTimeMillis
          //copy(model.copy(found = model.found + b.size), timeInMillis = currentTimeMillis)
          copy(model.copy(found = model.found + b.size), d)
      }
  }

  sealed trait PrivateCommand extends BaseMsg
  private[library] final case class GetCustomer(replyTo: ActorRef[CustomerModel])
      extends PrivateCommand

  def apply(): Behavior[Msg] =
    setup().narrow

  private[library] def testApply(): Behavior[BaseMsg] =
    setup()

  private[library] def setup(): Behavior[BaseMsg] =
    Behaviors.setup[BaseMsg] { context =>
      new Customer(context).receive()
    }

}

class Customer(context: ActorContext[RareBooksProtocol.BaseMsg]) {

  context.log.info("Customer started")

  import Customer._

  private var state = State(CustomerModel(0), -1L)

  protected def receive(): Behavior[RareBooksProtocol.BaseMsg] =
    Behaviors.receiveMessage {
      case b: RareBooksProtocol.BookFound =>
        context.log.info(f"${b.books.size}%d Book(s) found!")
        state = state.update(b)
        Behaviors.same
      case GetCustomer(replyTo) =>
        replyTo ! state.model
        Behaviors.same
      case m: RareBooksProtocol.Msg =>
        context.log.info(s"Received a message: ${m}")
        Behaviors.same
    }
}
