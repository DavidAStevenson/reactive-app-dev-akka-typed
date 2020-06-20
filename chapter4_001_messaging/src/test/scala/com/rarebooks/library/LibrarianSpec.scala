package com.rarebooks.library

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class LibrarianSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Receiving FindBookByTopic" should {

    import Catalog._
    import RareBooksProtocol._

    "result in BookFound, when the book exists" in {
      val customerProbe = testKit.createTestProbe[Msg]()
      val msg = FindBookByTopic(Set[Topic](Greece), customerProbe.ref)

      val librarian = spawn(Librarian(), "librarian")
      librarian ! msg

      val receive = BookFound(List[BookCard](phaedrus, theHistories))
      val result = customerProbe.expectMessageType[BookFound]
      result.books shouldBe receive.books
    }
  }
}
