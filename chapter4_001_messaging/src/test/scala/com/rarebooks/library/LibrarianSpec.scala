package com.rarebooks.library

import scala.concurrent.duration.{ MILLISECONDS => Millis, Duration, SECONDS }
import akka.actor.typed.{ Behavior }
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import com.typesafe.config.ConfigFactory

class LibrarianSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  import Catalog._
  import RareBooksProtocol._

  val conf = ConfigFactory.load()
  val findBookDuration = Duration(conf.getDuration("rare-books.open-duration", SECONDS), Millis)

  "Receiving FindBookByTitle" should {

    "result in BookFound, when the book exists" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val title = "The Epic of Gilgamesh"
      val msg = FindBookByTitle(title, customerProbe.ref)
      val librarian = spawn(Librarian(findBookDuration))
      librarian ! msg
      val result = customerProbe.expectMessageType[BookFound]
      result.books shouldBe List(theEpicOfGilgamesh)
    }

    "result in BookNotFound, when the book doesn't exist" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val title = "Swiss Family Robinson"
      val msg = FindBookByTitle(title, customerProbe.ref)
      val librarian = spawn(Librarian(findBookDuration))
      librarian ! msg
      val result = customerProbe.expectMessageType[BookNotFound]
      result.reason shouldBe s"No book(s) matching ${title}."
    }
  }

  "Receiving FindBookByTopic" should {

    "result in BookFound, when the book exists" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val msg = FindBookByTopic(Set[Topic](Greece), customerProbe.ref)

      val librarian = spawn(Librarian(findBookDuration))
      librarian ! msg

      val receive = BookFound(List[BookCard](phaedrus, theHistories))
      val result = customerProbe.expectMessageType[BookFound]
      result.books shouldBe receive.books
    }

    "result in BookNotFound, when the book doesn't exist" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val msg = FindBookByTopic(Set[Topic](Unknown), customerProbe.ref)

      val librarian = spawn(Librarian(findBookDuration))
      librarian ! msg

      val receive = BookNotFound(s"No book(s) matching ${msg.topic}.")
      val result = customerProbe.expectMessageType[BookNotFound]
      result.reason shouldBe receive.reason
    }
  }

  "Receiving FindBookByAuthor" should {

    "result in BookFound, when the book exists" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val author = "Herodotus"
      val msg = FindBookByAuthor(author, customerProbe.ref)
      val librarian = spawn(Librarian(findBookDuration))
      librarian ! msg
      val result = customerProbe.expectMessageType[BookFound]
      result.books shouldBe List(theHistories)
    }

    "result in BookNotFound, when the book doesn't exist" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val author = "Robert Luis Stevenson"
      val msg = FindBookByAuthor(author, customerProbe.ref)
      val librarian = spawn(Librarian(findBookDuration))
      librarian ! msg
      val result = customerProbe.expectMessageType[BookNotFound]
      result.reason shouldBe s"No book(s) matching ${author}."
    }
  }

  "Receiving FindBookByIsbn" should {

    "result in BookFound, when the book exists" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val isbn = "0872202208"
      val msg = FindBookByIsbn(isbn, customerProbe.ref)
      val librarian = spawn(Librarian(findBookDuration))
      librarian ! msg
      val result = customerProbe.expectMessageType[BookFound]
      result.books shouldBe List(phaedrus)
    }

    "result in BookNotFound, when the book doesn't exist" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val isbn = "0123456789"
      val msg = FindBookByIsbn(isbn, customerProbe.ref)
      val librarian = spawn(Librarian(findBookDuration))
      librarian ! msg
      val result = customerProbe.expectMessageType[BookNotFound]
      result.reason shouldBe s"No book(s) matching ${isbn}."
    }
  }

  "Receiving a FindBook request" should {

    def librarianTestApply(): Behavior[RareBooksProtocol.BaseMsg] =
      Librarian.setup(findBookDuration)

    "transition to busy state after receiving a request, when ready" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val isbn = "0123456789"
      val msg = FindBookByIsbn(isbn, customerProbe.ref)
      val librarian = spawn(librarianTestApply())

      val stateProbe = testKit.createTestProbe[Librarian.PrivateResponse]()
      librarian ! Librarian.GetState(stateProbe.ref)
      stateProbe.expectMessage(Librarian.Ready)

      librarian ! msg

      librarian ! Librarian.GetState(stateProbe.ref)
      stateProbe.expectMessage(Librarian.Busy)
    }

    "remain in busy state after receiving a request, when busy" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val isbn = "0123456789"
      val msg = FindBookByIsbn(isbn, customerProbe.ref)
      val librarian = spawn(librarianTestApply())

      val stateProbe = testKit.createTestProbe[Librarian.PrivateResponse]()
      librarian ! Librarian.GetState(stateProbe.ref)
      stateProbe.expectMessage(Librarian.Ready)

      librarian ! msg

      librarian ! Librarian.GetState(stateProbe.ref)
      stateProbe.expectMessage(Librarian.Busy)

      librarian ! msg

      librarian ! Librarian.GetState(stateProbe.ref)
      stateProbe.expectMessage(Librarian.Busy)
    }

    "stash a second request when received while busy" in {
      val customerProbe1 = testKit.createTestProbe[Msg]()
      val isbn1 = "0123456789"
      val msg1 = FindBookByIsbn(isbn1, customerProbe1.ref)
      val librarian = spawn(librarianTestApply())

      val stateProbe = testKit.createTestProbe[Librarian.PrivateResponse]()
      librarian ! Librarian.GetState(stateProbe.ref)
      stateProbe.expectMessage(Librarian.Ready)

      librarian ! msg1

      librarian ! Librarian.GetState(stateProbe.ref)
      stateProbe.expectMessage(Librarian.Busy)

      val customerProbe2 = testKit.createTestProbe[Msg]()
      val isbn2 = "0872202208"
      val msg2 = FindBookByIsbn(isbn2, customerProbe2.ref)
      librarian ! msg2

      librarian ! Librarian.GetState(stateProbe.ref)
      stateProbe.expectMessage(Librarian.Busy)

      val result1 = customerProbe1.expectMessageType[BookNotFound]
      result1.reason shouldBe s"No book(s) matching ${isbn1}."

      val result2 = customerProbe2.expectMessageType[BookFound]
      result2.books shouldBe List(phaedrus)
    }
  }
}
