package com.rarebooks.library

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.testkit.typed.scaladsl.LoggingTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class CustomerSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  import Catalog._
  import RareBooksProtocol._

  "Receiving BookFound" should {

    "log BookFound at info" in {
      val customer = spawn(Customer())
      val bookFound = BookFound(findBookByIsbn(theEpicOfGilgamesh.isbn).get)
      LoggingTestKit.info("1 Book(s) found!").expect {
        customer ! bookFound
      }
    }

    "increase Customer.model.bookFound by 1 for 1 book found" in {
      val customer = spawn(Customer.testApply())
      val bookFound = BookFound(findBookByIsbn(theEpicOfGilgamesh.isbn).get)
      customer ! bookFound
      val testProbe = testKit.createTestProbe[Customer.CustomerModel]()
      customer ! Customer.GetCustomer(testProbe.ref)
      testProbe.expectMessage(Customer.CustomerModel(1))
    }

    "increase Customer.model.bookFound by 2 for 2 books found" in {
      val customer = spawn(Customer.testApply())
      val bookFound = BookFound(findBookByTopic(Set(Greece)).get)
      customer ! bookFound
      val testProbe = testKit.createTestProbe[Customer.CustomerModel]()
      customer ! Customer.GetCustomer(testProbe.ref)
      testProbe.expectMessage(Customer.CustomerModel(2))
    }

    "increase Customer.model.bookFound for 2 BookFound messages (1+2 books)" in {
      val customer = spawn(Customer.testApply())
      val bookFound1 = BookFound(findBookByIsbn(theEpicOfGilgamesh.isbn).get)
      customer ! bookFound1
      val bookFound2 = BookFound(findBookByTopic(Set(Greece)).get)
      customer ! bookFound2
      val testProbe = testKit.createTestProbe[Customer.CustomerModel]()
      customer ! Customer.GetCustomer(testProbe.ref)
      testProbe.expectMessage(Customer.CustomerModel(3))
    }

  }
}
