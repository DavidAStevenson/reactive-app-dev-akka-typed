package com.rarebooks.library

import akka.actor.typed.{ ActorSystem, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }

object RareBooksApp {

  sealed trait Command
  case class CreateCustomer(count: Int, odds: Int, tolerance: Int) extends Command

  def apply(): Behavior[RareBooksApp.Command] =
    Behaviors.setup[Command] { context =>
      new RareBooksApp(context).run(0)
    }

  def main(args: Array[String]): Unit = {
    val actorSystem = ActorSystem[Command](RareBooksApp(), "RareBooksApp")
    val console = new RareBooksConsole(actorSystem)
    console.run()
  }
}

import RareBooksApp._

class RareBooksApp(context: ActorContext[Command]) {

  val rareBooks = context.spawn(RareBooks("rareBooks-R-us"), "rareBooks")

  private def run(nrOfCustomers: Int): Behavior[Command] =
    Behaviors.receiveMessage {
      case CreateCustomer(nrToCreate, odds, tolerance) if nrToCreate > 0 =>
        for (i <- nrOfCustomers until (nrOfCustomers + nrToCreate))
          context.spawn(Customer(rareBooks.ref, odds, tolerance), s"customer-${i}")
        run(nrOfCustomers + nrToCreate)
    }
}
