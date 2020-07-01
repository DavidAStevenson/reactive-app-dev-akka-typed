package com.rarebooks.library

import akka.actor.typed.ActorSystem
import scala.annotation.tailrec
import scala.io.StdIn

import RareBooksApp._

class RareBooksConsole(actorSystem: ActorSystem[Command]) extends Console {

  def run(): Unit = {
    println(
      f"${getClass.getSimpleName} running%nEnter commands [`q` = quit, `2c` = 2 customers, etc.]:"
    )

    commandLoop()
  }

  @tailrec
  private def commandLoop(): Unit = {
    println(actorSystem.printTree)
    Command(StdIn.readLine()) match {
      case Command.Customer(count, odds, tolerance) =>
        actorSystem ! CreateCustomer(count, odds, tolerance)
        commandLoop()
      case Command.Quit =>
        actorSystem.terminate()
      case Command.Unknown(command) =>
        println(s"Unknown command $command")
        commandLoop()
    }
  }
}
