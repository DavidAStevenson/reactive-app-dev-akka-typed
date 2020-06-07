package com.rarebooks.library

import org.scalatest.wordspec.AnyWordSpec
import java.lang.System.currentTimeMillis

class RareBooksProtocolSpec extends BaseSpec {

  import RareBooksProtocol._

  "Rare book protocol messages" should {
    "throw an IllegalArgumentException when 'BookFound.books' is empty" in {
      intercept[IllegalArgumentException] { BookFound(List()) }
    }
    "throw an IllegalArgumentException when 'BookNotFound.reason' is empty" in {
      intercept[IllegalArgumentException] { BookNotFound("") }
    }
    "have a dateInMillis no later than it is checked" in {
      assert(Complain().dateInMillis <= currentTimeMillis)
    }
    "throw an IllegalArgumentException when 'FindBookByAuthor.author' is empty" in {
      intercept[IllegalArgumentException] { FindBookByAuthor("") }
    }
    "throw an IllegalArgumentException when 'FindBookByIsbn.isbn' is empty" in {
      intercept[IllegalArgumentException] { FindBookByIsbn("") }
    }
    "throw an IllegalArgumentException when 'FindBookByTopic.topic' is empty" in {
      intercept[IllegalArgumentException] { FindBookByTopic(Set()) }
    }
    "throw an IllegalArgumentException when 'FindBookByTitle.title' is empty" in {
      intercept[IllegalArgumentException] { FindBookByTitle("") }
    }
  }

}
