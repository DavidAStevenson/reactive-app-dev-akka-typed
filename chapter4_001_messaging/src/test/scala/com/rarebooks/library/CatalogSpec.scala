package com.rarebooks.library

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

class CatalogSpec extends BaseSpec with TableDrivenPropertyChecks {

  import Catalog._
  import RareBooksProtocol._

  "books" should {
    "contain some example books" in {
      books.values.toSet should === (Set[BookCard](theEpicOfGilgamesh, phaedrus, theHistories))
    }
  }

  "findBookByIsbn" should {
    val findByIsbnCases = Table(
      ("statement", "isbn", "result")
      , ("theEpicOfGilgamesh", theEpicOfGilgamesh.isbn, Some[List[BookCard]](List(theEpicOfGilgamesh)) )
      , ("phaedrus"          , phaedrus.isbn          , Some[List[BookCard]](List(phaedrus))           )
      , ("theHistories"      , theHistories.isbn      , Some[List[BookCard]](List(theHistories))       )
      , ("None when isbn not found", "1234567890"     , None )
    )

    forAll(findByIsbnCases) { (statement: String, isbn: String, result: Option[List[BookCard]]) =>
      s"return $statement" in {
        findBookByIsbn(isbn) should === (result)
      }
    }
  }

}

