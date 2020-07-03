package com.rarebooks.library

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import scala.util.Random

import RareBooksProtocol._

object Customer {

  case class CustomerModel(odds: Int, tolerance: Int, found: Int, notFound: Int)
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

  def apply(rareBooks: ActorRef[Msg], odds: Int, tolerance: Int): Behavior[Msg] =
    setup(rareBooks, odds, tolerance).narrow

  private[library] def testApply(
      rareBooks: ActorRef[Msg],
      odds: Int,
      tolerance: Int
  ): Behavior[BaseMsg] =
    setup(rareBooks, odds, tolerance)

  private[library] def setup(
      rareBooks: ActorRef[Msg],
      odds: Int,
      tolerance: Int
  ): Behavior[BaseMsg] =
    Behaviors.setup[BaseMsg] { context =>
      new Customer(context, rareBooks, odds, tolerance).receive()
    }

}

class Customer(
    context: ActorContext[BaseMsg],
    rareBooks: ActorRef[Msg],
    odds: Int,
    tolerance: Int
) {

  import Customer._

  private var state = State(CustomerModel(odds, tolerance, 0, 0), -1L)

  // kick off
  context.log.info("Customer started")
  requestBookInfo()

  protected def receive(): Behavior[BaseMsg] =
    Behaviors.receiveMessage {
      case b: BookFound =>
        context.log.info(f"${b.books.size}%d Book(s) found!")
        state = state.update(b)
        requestBookInfo()
        Behaviors.same
      case b: BookNotFound if state.model.notFound < state.model.tolerance =>
        state = state.update(b)
        context.log.info(
          f"${state.model.notFound}%d not found so far, shocker! My tolerance is ${tolerance}%d"
        )
        requestBookInfo()
        Behaviors.same
      case b: BookNotFound =>
        state = state.update(b)
        context.log.info(
          f"${state.model.notFound}%d not found so far, shocker! My tolerance is ${tolerance}%d. Time to complain!"
        )
        b.replyTo ! Complain(context.self)
        Behaviors.same
      case c: Credit =>
        state = state.update(c)
        context.log.info("Credit received, will start requesting again!")
        requestBookInfo()
        Behaviors.same
      case GetCustomer(replyTo) =>
        replyTo ! state.model
        Behaviors.same
      case m: Msg =>
        context.log.info(s"Received a message: ${m}")
        Behaviors.same
    }

  private def requestBookInfo(): Unit =
    rareBooks ! FindBookByTopic(Set(pickTopic), context.self)

  private def pickTopic: Topic =
    if (Random.nextInt(100) < state.model.odds) viableTopics(Random.nextInt(viableTopics.size))
    else Unknown
}
