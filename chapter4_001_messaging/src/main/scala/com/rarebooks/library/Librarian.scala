package com.rarebooks.library

import akka.actor.typed.{ Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }

object Librarian {
  def apply(): Behavior[RareBooksProtocol.Msg] = Behaviors.setup { context =>
      new Librarian(context).ready()
  }
} 

class Librarian(context: ActorContext[RareBooksProtocol.Msg]) {

  context.log.info("Librarian started")

  import RareBooksProtocol._

  protected def ready(): Behavior[RareBooksProtocol.Msg] =
    Behaviors.receiveMessage {
      case FindBookByTopic(topic, replyTo, _) =>
        val result = Catalog.findBookByTopic(topic)
        result match {
          case Some(b) => replyTo ! BookFound(result.get)
          case None => replyTo ! BookNotFound(s"No book(s) matching ${topic}.")
        }
        Behaviors.same
      case FindBookByTitle(title, replyTo, _) =>
        val result = Catalog.findBookByTitle(title)
        result match {
          case Some(b) => replyTo ! BookFound(result.get)
          case None => replyTo ! BookNotFound(s"No book(s) matching ${title}.")
        }
        Behaviors.same
      case FindBookByAuthor(author, replyTo, _) =>
        val result = Catalog.findBookByAuthor(author)
        result match {
          case Some(b) => replyTo ! BookFound(result.get)
          case None => replyTo ! BookNotFound(s"No book(s) matching ${author}.")
        }
        Behaviors.same
      case FindBookByIsbn(isbn, replyTo, _) =>
        val result = Catalog.findBookByIsbn(isbn)
        result match {
          case Some(b) => replyTo ! BookFound(result.get)
          case None => replyTo ! BookNotFound(s"No book(s) matching ${isbn}.")
        }
        Behaviors.same
    }
  
}
