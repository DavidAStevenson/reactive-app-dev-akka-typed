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
import com.typesafe.config.ConfigFactory

class RareBooksSynchronousSpec extends BaseSpec {

  "Creating RareBooks" ignore {
    // synchronous testing not possible as RareBooks uses timers

    val childActorName = "librarian"

    s"spawn child actor named ${childActorName}" in {
      val testKit = BehaviorTestKit(RareBooks("rareBooks-synchronous"))
      testKit.expectEffectType[Spawned[Librarian]].childName should ===(childActorName)
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
    extends ScalaTestWithActorTestKit(ManualTime.config.withFallback(ConfigFactory.load()))
    with AnyWordSpecLike {

  val initLog = "RareBooks started"
  val openLog = "Time to open up!"
  val alreadyOpenLog = "We're already open."
  val closeLog = "Time to close!"
  val alreadyClosedLog = "We're already closed."
  val reportLog = "0 requests processed today. Total requests processed = 0"
  val reportLogOne = "1 requests processed today. Total requests processed = 1"
  val reportLogTwo = "1 requests processed today. Total requests processed = 2"

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

      val conf = ConfigFactory.load()
      val residualSecs = 1
      val openDurationSecs = conf.getDuration("rare-books.open-duration", SECONDS)
      val checkOpenDuration = openDurationSecs - residualSecs
      val closeDurationSecs = conf.getDuration("rare-books.close-duration", SECONDS)
      val checkClosedDuration = closeDurationSecs - residualSecs

      s"log '${alreadyOpenLog}' at info, when already opened" in {
        LoggingTestKit.info(alreadyOpenLog).expect {
          rareBooks ! RareBooks.Open
        }
      }

      s"stay open for at least ${checkOpenDuration} seconds" in {
        LoggingTestKit
          .info(closeLog)
          .withOccurrences(0)
          .expect {
            manualTime.timePasses(checkOpenDuration.seconds)
          }
      }

      "close down when it's time to close" in {
        LoggingTestKit.info(closeLog).expect {
          manualTime.timePasses(residualSecs.seconds)
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

      s"stay closed for at least ${checkClosedDuration} seconds" in {
        LoggingTestKit
          .info(openLog)
          .withOccurrences(0)
          .expect {
            manualTime.timePasses(checkClosedDuration.seconds)
          }
      }

      "re-open after being closed for a while" in {
        LoggingTestKit.info(openLog).expect {
          manualTime.timePasses(residualSecs.seconds)
        }
      }

      "make a report when it's time to close" in {
        LoggingTestKit.info(reportLog).expect {
          manualTime.timePasses(openDurationSecs.seconds)
        }
      }
    }

    "Sending FindBookByTopic" should {

      import RareBooksProtocol._

      "forward to librarian" in {
        val librarianProbe = testKit.createTestProbe[Msg]()
        val actorName = "rareBooks-sending1"
        val rareBooks = spawn(rareBooksTestApply(actorName), actorName)
        rareBooks ! RareBooks.ChangeLibrarian(librarianProbe.ref)

        val msg = FindBookByTopic(Set(Greece), system.ignoreRef)
        rareBooks ! msg
        librarianProbe.expectMessage(msg)
      }

      s"log '${reportLogOne}' at info after closing" in {
        val actorName = "rareBooks-sending2"
        val rareBooks = spawn(rareBooksTestApply(actorName), actorName)

        val msg = FindBookByTopic(Set(Greece), system.ignoreRef)
        rareBooks ! msg

        LoggingTestKit.info(reportLogOne).expect {
          rareBooks ! RareBooks.Close
        }
      }

      s"log '${reportLogTwo}' at info after two closes" in {
        val actorName = "rareBooks-sending3"
        val rareBooks = spawn(rareBooksTestApply(actorName), actorName)

        val msg = FindBookByTopic(Set(Greece), system.ignoreRef)
        rareBooks ! msg

        LoggingTestKit.info(reportLogOne).expect {
          rareBooks ! RareBooks.Close
        }

        rareBooks ! msg
        rareBooks ! RareBooks.Open

        LoggingTestKit.info(reportLogTwo).expect {
          rareBooks ! RareBooks.Close
        }

      }
    }

    "Stash messages" should {

      import RareBooksProtocol._

      val manualTime: ManualTime = ManualTime()

      val conf = ConfigFactory.load()
      val residualSecs = 1
      val closeDurationSecs = conf.getDuration("rare-books.close-duration", SECONDS)
      val checkClosedDuration = closeDurationSecs - residualSecs

      "stash messages while closed and process them after opening" in {
        val librarianProbe = testKit.createTestProbe[Msg]()
        val actorName = "rareBooks-stashing"
        val rareBooks = spawn(rareBooksTestApply(actorName), actorName)
        val msg = FindBookByTopic(Set(Greece), system.ignoreRef)

        rareBooks ! RareBooks.ChangeLibrarian(librarianProbe.ref)
        rareBooks ! RareBooks.Close
        rareBooks ! msg

        manualTime.expectNoMessageFor(checkClosedDuration.seconds, librarianProbe)
        manualTime.timePasses(residualSecs.seconds)
        librarianProbe.expectMessage(msg)
      }

      "log a warning when the stash is full" in {
        val actorName = "rareBooks-stashFull"
        val rareBooks = spawn(rareBooksTestApply(actorName))
        val msg = FindBookByTopic(Set(Greece), system.ignoreRef)
        val stashSize = conf.getInt("rare-books.stash-size")

        rareBooks ! RareBooks.Close

        for (i <- 1 to stashSize)
          rareBooks ! msg

        LoggingTestKit.warn("stash full while Closed, dropping new incoming message").expect {
          rareBooks ! msg
        }
      }
    }

  }
}
