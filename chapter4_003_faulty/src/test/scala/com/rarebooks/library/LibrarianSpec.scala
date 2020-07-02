package com.rarebooks.library

import scala.concurrent.duration.{ MILLISECONDS => Millis, Duration, SECONDS }
import akka.actor.typed.{ Behavior }
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.testkit.typed.scaladsl.LoggingTestKit
import akka.actor.testkit.typed.scaladsl.ManualTime
import org.slf4j.event.Level
import org.scalatest.wordspec.AnyWordSpecLike
import com.typesafe.config.ConfigFactory

class LibrarianSpec
    extends ScalaTestWithActorTestKit(ManualTime.config.withFallback(ConfigFactory.load()))
    with AnyWordSpecLike {

  import Catalog._
  import RareBooksProtocol._

  val conf = ConfigFactory.load()
  val findBookDuration =
    Duration(conf.getDuration("rare-books.librarian.find-book-duration", Millis), Millis)
  val stashSize = conf.getInt("rare-books.librarian.stash-size")

  val manualTime: ManualTime = ManualTime()

  def librarianTestApply(): Behavior[RareBooksProtocol.BaseMsg] =
    Librarian.setup(findBookDuration)

  "Receiving FindBookByTitle" should {

    "result in BookFound, when the book exists" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val title = "The Epic of Gilgamesh"
      val msg = FindBookByTitle(title, customerProbe.ref)
      val librarian = spawn(Librarian(findBookDuration))
      librarian ! msg

      manualTime.timePasses(findBookDuration)

      val result = customerProbe.expectMessageType[BookFound]
      result.books shouldBe List(theEpicOfGilgamesh)
    }

    "result in BookNotFound, when the book doesn't exist" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val title = "Swiss Family Robinson"
      val msg = FindBookByTitle(title, customerProbe.ref)
      val librarian = spawn(Librarian(findBookDuration))
      librarian ! msg

      manualTime.timePasses(findBookDuration)

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

      manualTime.timePasses(findBookDuration)

      val receive = BookFound(List[BookCard](phaedrus, theHistories))
      val result = customerProbe.expectMessageType[BookFound]
      result.books shouldBe receive.books
    }

    "result in BookNotFound, when the book doesn't exist" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val msg = FindBookByTopic(Set[Topic](Unknown), customerProbe.ref)

      val librarian = spawn(Librarian(findBookDuration))
      librarian ! msg

      manualTime.timePasses(findBookDuration)

      val receive = BookNotFound(s"No book(s) matching ${msg.topic}.", librarian.ref)
      val result = customerProbe.expectMessageType[BookNotFound]
      result.reason shouldBe receive.reason
      result.replyTo shouldBe librarian.ref
    }
  }

  "Receiving FindBookByAuthor" should {

    "result in BookFound, when the book exists" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val author = "Herodotus"
      val msg = FindBookByAuthor(author, customerProbe.ref)
      val librarian = spawn(Librarian(findBookDuration))
      librarian ! msg

      manualTime.timePasses(findBookDuration)

      val result = customerProbe.expectMessageType[BookFound]
      result.books shouldBe List(theHistories)
    }

    "result in BookNotFound, when the book doesn't exist" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val author = "Robert Luis Stevenson"
      val msg = FindBookByAuthor(author, customerProbe.ref)
      val librarian = spawn(Librarian(findBookDuration))
      librarian ! msg

      manualTime.timePasses(findBookDuration)

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

      manualTime.timePasses(findBookDuration)

      val result = customerProbe.expectMessageType[BookFound]
      result.books shouldBe List(phaedrus)
    }

    "result in BookNotFound, when the book doesn't exist" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val isbn = "0123456789"
      val msg = FindBookByIsbn(isbn, customerProbe.ref)
      val librarian = spawn(Librarian(findBookDuration))
      librarian ! msg

      manualTime.timePasses(findBookDuration)

      val result = customerProbe.expectMessageType[BookNotFound]
      result.reason shouldBe s"No book(s) matching ${isbn}."
    }
  }

  "Receiving a FindBook request" should {

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
      stateProbe.expectMessage(Librarian.Busy(1))
    }

    "transition to busy and back to ready state when processing a request" in {
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

      manualTime.timePasses(findBookDuration)

      val result = customerProbe.expectMessageType[BookNotFound]
      result.reason shouldBe s"No book(s) matching ${isbn}."

      librarian ! Librarian.GetState(stateProbe.ref)
      stateProbe.expectMessage(Librarian.Ready)
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
      stateProbe.expectMessage(Librarian.Busy(1))

      manualTime.timePasses(findBookDuration)

      val result1 = customerProbe1.expectMessageType[BookNotFound]
      result1.reason shouldBe s"No book(s) matching ${isbn1}."

      manualTime.timePasses(findBookDuration)

      val result2 = customerProbe2.expectMessageType[BookFound]
      result2.books shouldBe List(phaedrus)
    }

    "log a warning when the stash is full" in {
      val isbn = "0123456789"
      val msg = FindBookByIsbn(isbn, system.ignoreRef)
      val librarian = spawn(librarianTestApply())

      val stateProbe = testKit.createTestProbe[Librarian.PrivateResponse]()

      librarian ! Librarian.GetState(stateProbe.ref)
      stateProbe.expectMessage(Librarian.Ready)

      librarian ! msg

      librarian ! Librarian.GetState(stateProbe.ref)
      stateProbe.expectMessage(Librarian.Busy)

      for (i <- 1 to stashSize)
        librarian ! msg

      librarian ! Librarian.GetState(stateProbe.ref)
      stateProbe.expectMessage(Librarian.Busy(stashSize))

      LoggingTestKit.warn("stash full while busy, dropping new incoming message").expect {
        librarian ! msg
      }

    }
  }

  "Receiving a Complain" should {
    "log Credit issued to customer" in {
      val librarian = spawn(librarianTestApply())
      LoggingTestKit.info(s"Credit issued to customer ${system.ignoreRef}").expect {
        librarian ! Complain(system.ignoreRef)
      }
    }

    "send Credit to customer that sent Complain" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val librarian = spawn(librarianTestApply())
      librarian ! Complain(customerProbe.ref)
      customerProbe.expectMessageType[Credit]
    }

  }

}
