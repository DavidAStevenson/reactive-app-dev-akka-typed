package com.rarebooks.library

import akka.actor.typed.{ ActorSystem, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.NotUsed

object RareBooksApp {

  sealed trait Command
  case class CreateCustomer(nr: Int) extends Command

  def apply(): Behavior[RareBooksApp.Command] =
    Behaviors.setup[Command] { context =>
      new RareBooksApp(context).run(0)
    }

  def main(args: Array[String]): Unit = {
    val actorSystem = ActorSystem[Command](RareBooksApp(), "RareBooksApp")

    println(actorSystem.printTree)

    actorSystem ! CreateCustomer(1)
    println(actorSystem.printTree)

    Thread.sleep(5000) // ugh

    actorSystem ! CreateCustomer(5)
    println(actorSystem.printTree)

    Thread.sleep(15000) // ugh

    println(actorSystem.printTree)
    actorSystem.terminate()
  }
}

class RareBooksApp(context: ActorContext[RareBooksApp.Command]) {

  import RareBooksApp._

  val rareBooks = context.spawn(RareBooks("rareBooks-R-us"), "rareBooks")

  private def run(nrOfCustomers: Int): Behavior[Command] =
    Behaviors.receiveMessage {
      case CreateCustomer(nrToCreate) if nrToCreate > 0 =>
        for (i <- nrOfCustomers until (nrOfCustomers + nrToCreate))
          context.spawn(Customer(rareBooks.ref, 80, 5), s"customer-${i}")
      run(nrOfCustomers + nrToCreate)
    }
}
