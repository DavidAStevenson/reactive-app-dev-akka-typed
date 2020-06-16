package com.rarebooks.library

import scala.concurrent.duration._
import akka.actor.typed.{ Behavior }
import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.testkit.typed.Effect.{ Spawned }
import akka.actor.testkit.typed.scaladsl.BehaviorTestKit
import akka.actor.testkit.typed.scaladsl.TestInbox
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.testkit.typed.scaladsl.LoggingTestKit
import akka.actor.testkit.typed.scaladsl.ManualTime
import org.slf4j.event.Level
import org.scalatest.wordspec.AnyWordSpecLike

class RareBooksSynchronousSpec extends BaseSpec {

  "Creating RareBooks" ignore {
    // synchronous testing not possible as RareBooks uses timers

    val childActorName = "librarian"

    s"spawn child actor named ${childActorName}" in {
      val testKit = BehaviorTestKit(RareBooks("rareBooks-synchronous"))
      testKit.expectEffectType[Spawned[Librarian]].childName should === (childActorName)
    }

    "forward to librarian" in {
      import RareBooksProtocol._

      val testKit = BehaviorTestKit(RareBooks("rareBooks-synchronous"))
      val customer = TestInbox[Msg]()
      val msg = FindBookByTopic(Set(Greece), customer.ref)
      testKit.run(msg)
      var librarianInbox = testKit.childInbox[Msg](childActorName)
      librarianInbox.expectMessage(msg)
    }
  }
}

class RareBooksAsyncSpec
  extends ScalaTestWithActorTestKit(ManualTime.config)
  with AnyWordSpecLike {

  val initLog = "RareBooks started"
  val openLog = "Time to open up!"
  val alreadyOpenLog = "We're already open."
  val closeLog = "Time to close!"
  val alreadyClosedLog = "We're already closed."
  val reportLog = "Time to produce a report."

  // special version of apply() to enable testing of internals
  def rareBooksTestApply(name: String): Behavior[RareBooksProtocol.BaseMsg] =
    RareBooks.setup(name)


  "RareBooks" can {

    "initialize" should {

      val actorName = "rareBooks-init"
      s"log '${initLog}' when created" ignore {
        LoggingTestKit.info(s"${actorName}: ${initLog}").expect {
          val rareBooks = testKit.spawn(RareBooks(actorName), actorName)
        }
      }

    }

    "operate independently" should {

      val manualTime: ManualTime = ManualTime()
      val actorName = "rareBooks-operate"
      val rareBooks = spawn(rareBooksTestApply(actorName), actorName)

      "open up when initially commanded to open" ignore {
        LoggingTestKit.info(openLog).expect {
          rareBooks ! RareBooks.Open
        }
      }

      s"log '${alreadyOpenLog}' at info, when already opened" in {
        LoggingTestKit.info(alreadyOpenLog).expect {
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

      "close down when it's time to close and make a report" in {
        LoggingTestKit
          .empty
          .withLogLevel(Level.INFO)
          .withMessageRegex(s"[${closeLog}][${reportLog}]")
          .withOccurrences(2)
          .expect {
            manualTime.timePasses(1.seconds)
          }
      }

      s"log '${alreadyClosedLog}' at info, when already closed" in {
        LoggingTestKit.info(alreadyClosedLog).expect {
          rareBooks ! RareBooks.Close
        }
      }

      s"log '${reportLog}' at info, when a report command is received" in {
        LoggingTestKit.info(reportLog).expect {
          rareBooks ! RareBooks.Report
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

    "Sending FindBookByTopic" should {

      import RareBooksProtocol._

      val actorName = "rareBooks-sending"

      "forward to librarian" in {
        val librarianProbe = testKit.createTestProbe[Msg]()
        val rareBooks = spawn(rareBooksTestApply(actorName), actorName)
        rareBooks ! RareBooks.ChangeLibrarian(librarianProbe.ref)

        val customerProbe = testKit.createTestProbe[Msg]()
        val msg = FindBookByTopic(Set(Greece), customerProbe.ref)
        rareBooks ! msg
        librarianProbe.expectMessage(msg)
      }

    }

  }
}
