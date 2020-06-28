package com.rarebooks.library

import akka.actor.typed.{ ActorSystem, Behavior }
import akka.actor.typed.scaladsl.Behaviors
import akka.NotUsed

object RareBooksApp {

  def apply() =
    Behaviors.setup[Nothing] { context =>
      val rareBooks = context.spawn(RareBooks("rareBooks-R-us"), "rareBooks")

      val customer = context.spawn(Customer(rareBooks.ref, 80, 5), "customer1")

      Thread.sleep(60000) // ugh
      val system = context.system
      system.terminate()
      Behaviors.empty
    }

  def main(args: Array[String]): Unit = {
    ActorSystem[Nothing](RareBooksApp(), "RareBooksApp")
  }
}