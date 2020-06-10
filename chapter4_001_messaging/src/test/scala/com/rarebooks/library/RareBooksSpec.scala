package com.rarebooks.library

import scala.concurrent.duration._
import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.testkit.typed.scaladsl.BehaviorTestKit
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.testkit.typed.scaladsl.LoggingTestKit
import akka.actor.testkit.typed.scaladsl.ManualTime
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

class RareBooksAsyncSpec
  extends ScalaTestWithActorTestKit(ManualTime.config)
  with AnyWordSpecLike {

  val openLog = "Time to open up!"
  val closeLog = "Time to close!"
  val reportLog = "Time to produce a report."

  "RareBooks" can {

    "receive messages" should {

      val rareBooks = testKit.spawn(RareBooks(), "rareBooks")

      s"log '${openLog}' at info, when opened" in {
        LoggingTestKit.info(openLog).expect {
          rareBooks ! RareBooks.Open
        }
      }

      s"log '${closeLog}' at info, when closed" in {
        LoggingTestKit.info(closeLog).expect {
          rareBooks ! RareBooks.Close
        }
      }

      s"log '${reportLog}' at info, when a report command is received" in {
        LoggingTestKit.info(reportLog).expect {
          rareBooks ! RareBooks.Report
        }
      }
    }

    "operate independently" should {

      val manualTime: ManualTime = ManualTime()
      val rareBooks = spawn(RareBooks(), "rareBooks2")

      s"open up when initially commanded to open" in {
        LoggingTestKit.info(openLog).expect {
          rareBooks ! RareBooks.Open
        }
      }

      "stay open for at least 9 seconds" in {
        LoggingTestKit
          .info(closeLog)
          .withOccurrences(0).expect {
            manualTime.timePasses(9.seconds)
          }
      }

      s"close down when it's time to close" in {
        LoggingTestKit.info(closeLog).expect {
          manualTime.timePasses(1.seconds)
        }
      }

      "stay closed for at least 9 seconds" in {
        LoggingTestKit
          .info(openLog)
          .withOccurrences(0).expect {
            manualTime.timePasses(9.seconds)
          }
      }

      "re-open after being closed for a while" in {
        LoggingTestKit.info(openLog).expect {
          manualTime.timePasses(1.seconds)
        }
      }
    }
  }
}
