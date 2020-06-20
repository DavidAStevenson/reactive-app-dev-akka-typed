package com.rarebooks.library

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class LibrarianSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  import Catalog._
  import RareBooksProtocol._

  "Receiving FindBookByTitle" should {
    "result in BookFound, when the book exists" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val title = "The Epic of Gilgamesh"
      val msg = FindBookByTitle(title, customerProbe.ref)
      val librarian = spawn(Librarian())
      librarian ! msg
      val result = customerProbe.expectMessageType[BookFound]
      result.books shouldBe List(theEpicOfGilgamesh)
    }

    "result in BookNotFound, when the book doesn't exist" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val title = "Swiss Family Robinson"
      val msg = FindBookByTitle(title, customerProbe.ref)
      val librarian = spawn(Librarian())
      librarian ! msg
      val result = customerProbe.expectMessageType[BookNotFound]
      result.reason shouldBe "No book(s) matching Swiss Family Robinson."
    }
  }

  "Receiving FindBookByTopic" should {

    "result in BookFound, when the book exists" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val msg = FindBookByTopic(Set[Topic](Greece), customerProbe.ref)

      val librarian = spawn(Librarian())
      librarian ! msg

      val receive = BookFound(List[BookCard](phaedrus, theHistories))
      val result = customerProbe.expectMessageType[BookFound]
      result.books shouldBe receive.books
    }

    "result in BookNotFound, when the book doesn't exist" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val msg = FindBookByTopic(Set[Topic](Unknown), customerProbe.ref)

      val librarian = spawn(Librarian())
      librarian ! msg

      val receive = BookNotFound(s"No book(s) matching ${msg.topic}.")
      val result = customerProbe.expectMessageType[BookNotFound]
      result.reason shouldBe receive.reason
    }
  }
}
