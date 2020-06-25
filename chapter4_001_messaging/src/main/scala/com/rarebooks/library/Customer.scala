package com.rarebooks.library

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }

object Customer {

  import RareBooksProtocol._

  case class CustomerModel(tolerance: Int, found: Int, notFound: Int)
  private case class State(model: CustomerModel, timeInMillis: Long) {
    def update(m: Msg): State =
      m match {
        case BookFound(b, d) =>
          //import java.lang.System.currentTimeMillis
          //copy(model.copy(found = model.found + b.size), timeInMillis = currentTimeMillis)
          copy(model.copy(found = model.found + b.size), timeInMillis = d)
        case BookNotFound(_, d) =>
          copy(model.copy(notFound = model.notFound + 1), timeInMillis = d)
      }
  }

  sealed trait PrivateCommand extends BaseMsg
  private[library] final case class GetCustomer(replyTo: ActorRef[CustomerModel])
      extends PrivateCommand

  def apply(tolerance: Int): Behavior[Msg] =
    setup(tolerance).narrow

  private[library] def testApply(): Behavior[BaseMsg] =
    setup()

  private[library] def setup(tolerance: Int = 5): Behavior[BaseMsg] =
    Behaviors.setup[BaseMsg] { context =>
      new Customer(context, tolerance).receive()
    }

}

class Customer(context: ActorContext[RareBooksProtocol.BaseMsg], tolerance: Int) {

  context.log.info("Customer started")

  import Customer._

  private var state = State(CustomerModel(tolerance, 0, 0), -1L)

  protected def receive(): Behavior[RareBooksProtocol.BaseMsg] =
    Behaviors.receiveMessage {
      case b: RareBooksProtocol.BookFound =>
        context.log.info(f"${b.books.size}%d Book(s) found!")
        state = state.update(b)
        Behaviors.same
      case b: RareBooksProtocol.BookNotFound if state.model.notFound < state.model.tolerance =>
        state = state.update(b)
        context.log.info(
          f"${state.model.notFound}%d not found so far, shocker! My tolerance is ${tolerance}%d"
        )
        Behaviors.same
      case GetCustomer(replyTo) =>
        replyTo ! state.model
        Behaviors.same
      case m: RareBooksProtocol.Msg =>
        context.log.info(s"Received a message: ${m}")
        Behaviors.same
    }
}
