package com.rarebooks.library

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import scala.util.Random

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
        case BookNotFound(_, _, d) =>
          copy(model.copy(notFound = model.notFound + 1), timeInMillis = d)
        case Credit(d) =>
          copy(model.copy(notFound = 0), timeInMillis = d)
      }
  }

  sealed trait PrivateCommand extends BaseMsg
  private[library] final case class GetCustomer(replyTo: ActorRef[CustomerModel])
      extends PrivateCommand

  def apply(rareBooks: ActorRef[RareBooksProtocol.Msg], tolerance: Int): Behavior[Msg] =
    setup(rareBooks, tolerance).narrow

  private[library] def testApply(
      rareBooks: ActorRef[RareBooksProtocol.Msg],
      tolerance: Int
  ): Behavior[BaseMsg] =
    setup(rareBooks, tolerance)

  private[library] def setup(
      rareBooks: ActorRef[RareBooksProtocol.Msg],
      tolerance: Int
  ): Behavior[BaseMsg] =
    Behaviors.setup[BaseMsg] { context =>
      new Customer(context, rareBooks, tolerance).receive()
    }

}

class Customer(
    context: ActorContext[RareBooksProtocol.BaseMsg],
    rareBooks: ActorRef[RareBooksProtocol.Msg],
    tolerance: Int
) {

  import Customer._
  import RareBooksProtocol._

  private var state = State(CustomerModel(tolerance, 0, 0), -1L)

  // kick off
  context.log.info("Customer started")
  requestBookInfo()

  protected def receive(): Behavior[RareBooksProtocol.BaseMsg] =
    Behaviors.receiveMessage {
      case b: RareBooksProtocol.BookFound =>
        context.log.info(f"${b.books.size}%d Book(s) found!")
        state = state.update(b)
        requestBookInfo()
        Behaviors.same
      case b: RareBooksProtocol.BookNotFound if state.model.notFound < state.model.tolerance =>
        state = state.update(b)
        context.log.info(
          f"${state.model.notFound}%d not found so far, shocker! My tolerance is ${tolerance}%d"
        )
        requestBookInfo()
        Behaviors.same
      case b: RareBooksProtocol.BookNotFound =>
        state = state.update(b)
        context.log.info(
          f"${state.model.notFound}%d not found so far, shocker! My tolerance is ${tolerance}%d. Time to complain!"
        )
        b.replyTo ! Complain(context.self)
        Behaviors.same
      case c: Credit =>
        state = state.update(c)
        Behaviors.same
      case GetCustomer(replyTo) =>
        replyTo ! state.model
        Behaviors.same
      case m: RareBooksProtocol.Msg =>
        context.log.info(s"Received a message: ${m}")
        Behaviors.same
    }

  private def requestBookInfo(): Unit =
    rareBooks ! FindBookByTopic(Set(pickTopic), context.self)

  private def pickTopic: Topic =
    if (Random.nextInt(100) < 50) viableTopics(Random.nextInt(viableTopics.size)) else Unknown
}
