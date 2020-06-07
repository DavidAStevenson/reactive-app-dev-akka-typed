package com.rarebooks.library

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class CatalogSpec extends BaseSpec {

  import Catalog._
  import RareBooksProtocol._

  "books" should {
    "contain some example books" in {
      books.values.toSet should === (Set[BookCard](theEpicOfGilgamesh, phaedrus, theHistories))
    }
  }

}

