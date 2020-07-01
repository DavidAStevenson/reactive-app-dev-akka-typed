package com.rarebooks.library

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.testkit.typed.scaladsl.LoggingTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class CustomerSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  import Catalog._
  import RareBooksProtocol._

  private val OddsZero: Int = 0
  private val OddsCertain: Int = 100
  private val ToleranceNonZero: Int = 5
  private val ToleranceZero: Int = 0

  "Receiving BookFound" should {

    "log BookFound at info" in {
      val customer = spawn(Customer(system.ignoreRef, OddsCertain, ToleranceNonZero))
      val bookFound = BookFound(findBookByIsbn(theEpicOfGilgamesh.isbn).get)
      LoggingTestKit.info("1 Book(s) found!").expect {
        customer ! bookFound
      }
    }

    "send another FindBookByTopic message" in {
      val rarebooks = testKit.createTestProbe[Msg]()
      val customer = spawn(Customer.testApply(rarebooks.ref, OddsCertain, ToleranceNonZero))
      rarebooks.expectMessageType[FindBookByTopic]
      customer ! BookFound(findBookByIsbn(theEpicOfGilgamesh.isbn).get)
      rarebooks.expectMessageType[FindBookByTopic]
    }

    "increase Customer.model.bookFound by 1 for 1 book found" in {
      val rarebooks = testKit.createTestProbe[Msg]()
      val customer = spawn(Customer.testApply(rarebooks.ref, OddsCertain, ToleranceNonZero))
      rarebooks.expectMessageType[FindBookByTopic]
      val bookFound = BookFound(findBookByIsbn(theEpicOfGilgamesh.isbn).get)
      customer ! bookFound
      val testProbe = testKit.createTestProbe[Customer.CustomerModel]()
      customer ! Customer.GetCustomer(testProbe.ref)
      testProbe.expectMessage(Customer.CustomerModel(OddsCertain, ToleranceNonZero, 1, 0))
    }

    "increase Customer.model.bookFound by 2 for 2 books found" in {
      val rarebooks = testKit.createTestProbe[Msg]()
      val customer = spawn(Customer.testApply(rarebooks.ref, OddsCertain, ToleranceNonZero))
      rarebooks.expectMessageType[FindBookByTopic]
      val bookFound = BookFound(findBookByTopic(Set(Greece)).get)
      customer ! bookFound
      val testProbe = testKit.createTestProbe[Customer.CustomerModel]()
      customer ! Customer.GetCustomer(testProbe.ref)
      testProbe.expectMessage(Customer.CustomerModel(OddsCertain, ToleranceNonZero, 2, 0))
    }

    "increase Customer.model.bookFound for 2 BookFound messages (1+2 books)" in {
      val rarebooks = testKit.createTestProbe[Msg]()
      val customer = spawn(Customer.testApply(rarebooks.ref, OddsCertain, ToleranceNonZero))
      rarebooks.expectMessageType[FindBookByTopic]
      val bookFound1 = BookFound(findBookByIsbn(theEpicOfGilgamesh.isbn).get)
      customer ! bookFound1
      val bookFound2 = BookFound(findBookByTopic(Set(Greece)).get)
      customer ! bookFound2
      val testProbe = testKit.createTestProbe[Customer.CustomerModel]()
      customer ! Customer.GetCustomer(testProbe.ref)
      testProbe.expectMessage(Customer.CustomerModel(OddsCertain, ToleranceNonZero, 3, 0))
    }

  }

  "Receiving BookNotFound with not found count less than tolerance" should {

    "log BookNotFound at info" in {
      val customer = spawn(Customer(system.ignoreRef, OddsZero, ToleranceNonZero))
      LoggingTestKit
        .info(f"1 not found so far, shocker! My tolerance is ${ToleranceNonZero}%d")
        .expect {
          customer ! BookNotFound("We don't have such type of books!", system.ignoreRef)
        }
    }

    "increase Customer.model.bookNotFound by 1 for 1 book not found" in {
      val rarebooks = testKit.createTestProbe[Msg]()
      val customer = spawn(Customer.testApply(rarebooks.ref, OddsZero, ToleranceNonZero))
      rarebooks.expectMessageType[FindBookByTopic]
      val bookNotFound = BookNotFound("We don't have such type of books!", system.ignoreRef)
      customer ! bookNotFound
      val testProbe = testKit.createTestProbe[Customer.CustomerModel]()
      customer ! Customer.GetCustomer(testProbe.ref)
      testProbe.expectMessage(Customer.CustomerModel(OddsZero, ToleranceNonZero, 0, 1))
    }

    "send another FindBookByTopic message" in {
      val rarebooks = testKit.createTestProbe[Msg]()
      val customer = spawn(Customer.testApply(rarebooks.ref, OddsZero, ToleranceNonZero))
      rarebooks.expectMessageType[FindBookByTopic]
      customer ! BookNotFound("No books like that.", system.ignoreRef)
      rarebooks.expectMessageType[FindBookByTopic]
    }

  }

  "Receiving BookNotFound with not found count at tolerance level or more" should {

    "log BookNotFound at info, with tolerance reached" in {
      val customer = spawn(Customer(system.ignoreRef, OddsZero, ToleranceZero))
      LoggingTestKit
        .info(f"1 not found so far, shocker! My tolerance is ${ToleranceZero}%d. Time to complain!")
        .expect {
          customer ! BookNotFound("We don't have such type of books!", system.ignoreRef)
        }
    }

    "increase Customer.model.bookNotFound by 1 for 1 book not found" in {
      val rarebooks = testKit.createTestProbe[Msg]()
      val customer = spawn(Customer.testApply(rarebooks.ref, OddsZero, ToleranceZero))
      rarebooks.expectMessageType[FindBookByTopic]
      val bookNotFound = BookNotFound("We don't have such type of books!", system.ignoreRef)
      customer ! bookNotFound
      val testProbe = testKit.createTestProbe[Customer.CustomerModel]()
      customer ! Customer.GetCustomer(testProbe.ref)
      testProbe.expectMessage(Customer.CustomerModel(OddsZero, ToleranceZero, 0, 1))
    }

    "send a Complaint to the Librarian" in {
      val rarebooks = testKit.createTestProbe[Msg]()
      val customer = spawn(Customer.testApply(rarebooks.ref, OddsZero, ToleranceZero))
      rarebooks.expectMessageType[FindBookByTopic]
      val librarian = testKit.createTestProbe[Msg]()
      customer ! BookNotFound("We don't have such type of books!", librarian.ref)
      librarian.expectMessageType[Complain]
    }

    "stop sending FindBook requests" in {
      val rarebooks = testKit.createTestProbe[Msg]()
      val customer = spawn(Customer.testApply(rarebooks.ref, OddsZero, ToleranceZero))
      rarebooks.expectMessageType[FindBookByTopic]
      customer ! BookNotFound("We don't have such type of books!", system.ignoreRef)
      rarebooks.expectNoMessage()
    }

  }

  "Receiving Credit" should {

    "reset the Customer's tolerance" in {
      val rarebooks = testKit.createTestProbe[Msg]()
      val customer = spawn(Customer.testApply(rarebooks.ref, OddsZero, ToleranceZero))
      rarebooks.expectMessageType[FindBookByTopic]

      val librarian = testKit.createTestProbe[Msg]()
      customer ! BookNotFound("We don't have such type of books!", librarian.ref)
      librarian.expectMessageType[Complain]

      val testProbe = testKit.createTestProbe[Customer.CustomerModel]()
      customer ! Customer.GetCustomer(testProbe.ref)
      testProbe.expectMessage(Customer.CustomerModel(OddsZero, ToleranceZero, 0, 1))

      customer ! Credit()

      customer ! Customer.GetCustomer(testProbe.ref)
      testProbe.expectMessage(Customer.CustomerModel(OddsZero, ToleranceZero, 0, 0))
    }

    "make the Customer resume sending FindBook requests" in {
      val rarebooks = testKit.createTestProbe[Msg]()
      val customer = spawn(Customer.testApply(rarebooks.ref, OddsZero, ToleranceZero))
      rarebooks.expectMessageType[FindBookByTopic]

      val librarian = testKit.createTestProbe[Msg]()
      customer ! BookNotFound("We don't have such type of books!", librarian.ref)
      librarian.expectMessageType[Complain]

      val testProbe = testKit.createTestProbe[Customer.CustomerModel]()
      customer ! Customer.GetCustomer(testProbe.ref)
      testProbe.expectMessage(Customer.CustomerModel(OddsZero, ToleranceZero, 0, 1))

      customer ! Credit()

      rarebooks.expectMessageType[FindBookByTopic]
    }
  }

  "Picking Topic" should {
    "pick Unknown when Customer state has zero odds" in {
      val rarebooks = testKit.createTestProbe[Msg]()
      val customer = spawn(Customer.testApply(rarebooks.ref, OddsZero, ToleranceZero))
      val msg = rarebooks.expectMessageType[FindBookByTopic]
      msg.topic shouldBe Set(Unknown)
    }

    "pick a viableTopic when Customer state has certain odds" in {
      val rarebooks = testKit.createTestProbe[Msg]()
      val customer = spawn(Customer.testApply(rarebooks.ref, OddsCertain, ToleranceZero))
      val msg = rarebooks.expectMessageType[FindBookByTopic]
      msg.topic should not be empty
      msg.topic.foreach(e => RareBooksProtocol.viableTopics should contain(e))
    }

  }
}
