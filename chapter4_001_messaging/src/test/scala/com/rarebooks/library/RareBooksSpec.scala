package com.rarebooks.library

import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.testkit.typed.scaladsl.BehaviorTestKit
import org.slf4j.event.Level

class RareBooksSpec extends BaseSpec {

  "Creating RareBooks" should {

    "log \"RareBooks started\" when created" in {
      val testKit = BehaviorTestKit(RareBooks())
      testKit.logEntries() shouldBe Seq(CapturedLogEvent(Level.INFO, "RareBooks started"))
    }
  }
}
