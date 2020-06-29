package com.rarebooks.library

import akka.actor.typed.{ ActorSystem, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.NotUsed
import scala.annotation.tailrec
import scala.io.StdIn

object RareBooksApp {

  sealed trait Command
  case class CreateCustomer(nr: Int) extends Command

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
      case CreateCustomer(nrToCreate) if nrToCreate > 0 =>
        for (i <- nrOfCustomers until (nrOfCustomers + nrToCreate))
          context.spawn(Customer(rareBooks.ref, 80, 5), s"customer-${i}")
      run(nrOfCustomers + nrToCreate)
    }
}

class RareBooksConsole(actorSystem: ActorSystem[Command]) extends Console {

  def run(): Unit = {
    println(actorSystem.printTree)

    println(f"{} running%nEnter commands [`q` = quit, `2c` = 2 customers, etc.]:", getClass.getSimpleName)

    commandLoop()
  }

  @tailrec
  private def commandLoop(): Unit = {
    println(actorSystem.printTree)
    Command(StdIn.readLine()) match {
      case Command.Customer(count, odds, tolerance) =>
        actorSystem ! CreateCustomer(count)
        commandLoop()
      case Command.Quit =>
        actorSystem.terminate()
      case Command.Unknown(command) =>
        println(s"Unknown command $command")
        commandLoop()
    }
  }
}
