package com.rarebooks.library

import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.testkit.typed.scaladsl.BehaviorTestKit
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.testkit.typed.scaladsl.LoggingTestKit
import org.slf4j.event.Level
import org.scalatest.wordspec.AnyWordSpecLike

class RareBooksSynchronousSpec extends BaseSpec {

  "Creating RareBooks" should {

    "log \"RareBooks started\" when created" in {
      val testKit = BehaviorTestKit(RareBooks())
      testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.INFO, "RareBooks started"))
    }
  }
}

class RareBooksAsyncSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "RareBooks" should {
    "log \"Time to open up!\" at info, when opened" in {
      val rareBooks = testKit.spawn(RareBooks(), "rareBooks")
      LoggingTestKit.info("Time to open up!").expect {
        rareBooks ! RareBooks.Open
      }
    }
  }

}
