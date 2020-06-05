package com.rarebooks.library

import org.scalatest.wordspec.AnyWordSpec

class RareBooksProtocolSpec extends AnyWordSpec {

  import RareBooksProtocol._

  "Rare book protocol messages" should {
    "throw an IllegalArgumentException when 'BookFound.books' is empty" in {
      intercept[IllegalArgumentException] { BookFound(List()) }
    }
  }

}
