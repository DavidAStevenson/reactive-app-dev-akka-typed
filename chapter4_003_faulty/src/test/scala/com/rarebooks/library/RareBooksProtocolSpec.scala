package com.rarebooks.library

import org.scalatest.wordspec.AnyWordSpec
import java.lang.System.currentTimeMillis
import akka.actor.testkit.typed.scaladsl.TestInbox
import akka.actor.typed.ActorRef

class RareBooksProtocolSpec extends BaseSpec {

  import RareBooksProtocol._

  "Rare book protocol messages" should {
    val dummyRef: ActorRef[Msg] = TestInbox[Msg]().ref

    "throw an IllegalArgumentException when 'BookFound.books' is empty" in {
      intercept[IllegalArgumentException] { BookFound(List()) }
    }
    "throw an IllegalArgumentException when 'BookNotFound.reason' is empty" in {
      intercept[IllegalArgumentException] { BookNotFound("", dummyRef) }
    }
    "have a dateInMillis no later than it is checked" in {
      assert(Complain(dummyRef).dateInMillis <= currentTimeMillis)
    }
    "throw an IllegalArgumentException when 'FindBookByAuthor.author' is empty" in {
      intercept[IllegalArgumentException] { FindBookByAuthor("", dummyRef) }
    }
    "throw an IllegalArgumentException when 'FindBookByIsbn.isbn' is empty" in {
      intercept[IllegalArgumentException] { FindBookByIsbn("", dummyRef) }
    }
    "throw an IllegalArgumentException when 'FindBookByTopic.topic' is empty" in {
      intercept[IllegalArgumentException] { FindBookByTopic(Set(), dummyRef) }
    }
    "throw an IllegalArgumentException when 'FindBookByTitle.title' is empty" in {
      intercept[IllegalArgumentException] { FindBookByTitle("", dummyRef) }
    }
  }

}
