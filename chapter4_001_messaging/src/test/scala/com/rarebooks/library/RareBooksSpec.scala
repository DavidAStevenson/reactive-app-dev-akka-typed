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

    val rareBooks = testKit.spawn(RareBooks(), "rareBooks")

    val openLog = "Time to open up!"
    s"log '${openLog}' at info, when opened" in {
      LoggingTestKit.info(openLog).expect {
        rareBooks ! RareBooks.Open
      }
    }

    val closeLog = "Time to close!"
    s"log '${closeLog}' at info, when closed" in {
      LoggingTestKit.info(closeLog).expect {
        rareBooks ! RareBooks.Close
      }
    }

    val reportLog = "Time to produce a report."
    s"log '${reportLog}' at info, when a report command is received" in {
      LoggingTestKit.info(reportLog).expect {
        rareBooks ! RareBooks.Report
      }
    }

  }

}
