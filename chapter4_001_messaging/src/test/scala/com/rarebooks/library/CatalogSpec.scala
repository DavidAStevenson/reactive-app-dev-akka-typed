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
      s"return $statement when searching by ISBN($isbn)" in {
        findBookByIsbn(isbn) should === (result)
      }
    }
  }

  "findBookByAuthor" should {
    val findByAuthorCases = Table(
      ("statement", "author", "result")
      , ("theEpicOfGilgamesh", theEpicOfGilgamesh.author, Some[List[BookCard]](List(theEpicOfGilgamesh)) )
      , ("phaedrus"          , phaedrus.author          , Some[List[BookCard]](List(phaedrus))           )
      , ("theHistories"      , theHistories.author      , Some[List[BookCard]](List(theHistories))       )
      , ("None when author not found", "Williard Price" , None)
      )

    forAll(findByAuthorCases) { (statement: String, author: String, result: Option[List[BookCard]]) =>
      s"return $statement when searching by author($author)" in {
        findBookByAuthor(author) should === (result)
      }
    }
  }

  "findBookByTitle" should {
    val findByTitleCases = Table(
      ("statement", "title", "result")
      , ("theEpicOfGilgamesh", theEpicOfGilgamesh.title, Some[List[BookCard]](List(theEpicOfGilgamesh)) )
      , ("phaedrus"          , phaedrus.title          , Some[List[BookCard]](List(phaedrus))           )
      , ("theHistories"      , theHistories.title      , Some[List[BookCard]](List(theHistories))       )
      , ("None when title not found", "Whale Adventure" , None)
      )

    forAll(findByTitleCases) { (statement: String, title: String, result: Option[List[BookCard]]) =>
      s"return $statement when searching by title($title)" in {
        findBookByTitle(title) should === (result)
      }
    }
  }

  "findBookByTopic" should {
    val findByTopicCases = Table(
      ("statement", "topics", "result")
      , ("theEpicOfGilgamesh", (Set[Topic](Gilgamesh, Persia, Royalty)), Some[List[BookCard]](List(theEpicOfGilgamesh)))
      , ("phaedrus", Set[Topic](Philosophy), Some[List[BookCard]](List(phaedrus)))
      , ("phaedrus and theHistories", Set[Topic](Greece, Philosophy), Some[List[BookCard]](List(phaedrus, theHistories)))
      , ("theHistories", Set[Topic](Africa, Asia, Greece, Tradition), Some[List[BookCard]](List(phaedrus, theHistories)))
      , ("theHistories", Set[Topic](Tradition), Some[List[BookCard]](List(theHistories)))
      , ("None", Set[Topic](Unknown), None)
      )

    forAll(findByTopicCases) { (statement: String, topics: Set[Topic], result: Option[List[BookCard]]) =>
      s"return $statement when searching by topics: ($topics)" in {
        findBookByTopic(topics) should === (result)
      }
    }

  }

}

