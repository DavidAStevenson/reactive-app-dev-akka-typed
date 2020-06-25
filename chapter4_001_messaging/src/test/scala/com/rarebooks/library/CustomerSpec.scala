package com.rarebooks.library

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.testkit.typed.scaladsl.LoggingTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class CustomerSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  import Catalog._
  import RareBooksProtocol._

  private val ToleranceNonZero: Int = 5

  "Receiving BookFound" should {

    "log BookFound at info" in {
      val customer = spawn(Customer(system.deadLetters, ToleranceNonZero))
      val bookFound = BookFound(findBookByIsbn(theEpicOfGilgamesh.isbn).get)
      LoggingTestKit.info("1 Book(s) found!").expect {
        customer ! bookFound
      }
    }

    "increase Customer.model.bookFound by 1 for 1 book found" in {
      val rarebooks = testKit.createTestProbe[RareBooksProtocol.Msg]
      val customer = spawn(Customer.testApply(rarebooks.ref))
      rarebooks.expectMessageType[RareBooksProtocol.FindBookByTopic]
      val bookFound = BookFound(findBookByIsbn(theEpicOfGilgamesh.isbn).get)
      customer ! bookFound
      val testProbe = testKit.createTestProbe[Customer.CustomerModel]()
      customer ! Customer.GetCustomer(testProbe.ref)
      testProbe.expectMessage(Customer.CustomerModel(ToleranceNonZero, 1, 0))
    }

    "increase Customer.model.bookFound by 2 for 2 books found" in {
      val rarebooks = testKit.createTestProbe[RareBooksProtocol.Msg]
      val customer = spawn(Customer.testApply(rarebooks.ref))
      rarebooks.expectMessageType[RareBooksProtocol.FindBookByTopic]
      val bookFound = BookFound(findBookByTopic(Set(Greece)).get)
      customer ! bookFound
      val testProbe = testKit.createTestProbe[Customer.CustomerModel]()
      customer ! Customer.GetCustomer(testProbe.ref)
      testProbe.expectMessage(Customer.CustomerModel(ToleranceNonZero, 2, 0))
    }

    "increase Customer.model.bookFound for 2 BookFound messages (1+2 books)" in {
      val rarebooks = testKit.createTestProbe[RareBooksProtocol.Msg]
      val customer = spawn(Customer.testApply(rarebooks.ref))
      rarebooks.expectMessageType[RareBooksProtocol.FindBookByTopic]
      val bookFound1 = BookFound(findBookByIsbn(theEpicOfGilgamesh.isbn).get)
      customer ! bookFound1
      val bookFound2 = BookFound(findBookByTopic(Set(Greece)).get)
      customer ! bookFound2
      val testProbe = testKit.createTestProbe[Customer.CustomerModel]()
      customer ! Customer.GetCustomer(testProbe.ref)
      testProbe.expectMessage(Customer.CustomerModel(ToleranceNonZero, 3, 0))
    }

  }

  "Receiving BookNotFound with not found count less than tolerance" should {

    "log BookNotFound at info" in {
      val customer = spawn(Customer(system.deadLetters, ToleranceNonZero))
      LoggingTestKit
        .info(f"1 not found so far, shocker! My tolerance is ${ToleranceNonZero}%d")
        .expect {
          customer ! BookNotFound("We don't have such type of books!")
        }
    }

    "increase Customer.model.bookNotFound by 1 for 1 book not found" in {
      val rarebooks = testKit.createTestProbe[RareBooksProtocol.Msg]
      val customer = spawn(Customer.testApply(rarebooks.ref))
      rarebooks.expectMessageType[RareBooksProtocol.FindBookByTopic]
      val bookNotFound = BookNotFound("We don't have such type of books!")
      customer ! bookNotFound
      val testProbe = testKit.createTestProbe[Customer.CustomerModel]()
      customer ! Customer.GetCustomer(testProbe.ref)
      testProbe.expectMessage(Customer.CustomerModel(ToleranceNonZero, 0, 1))
    }

    "send another FindBookByTopic message" in {
      val rarebooks = testKit.createTestProbe[RareBooksProtocol.Msg]
      val customer = spawn(Customer.testApply(rarebooks.ref))
      rarebooks.expectMessageType[RareBooksProtocol.FindBookByTopic]
      customer ! BookNotFound("No books like that.")
      rarebooks.expectMessageType[RareBooksProtocol.FindBookByTopic]
    }

  }
}
