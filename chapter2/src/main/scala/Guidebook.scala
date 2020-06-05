import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ AbstractBehavior, ActorContext, Behaviors }
import akka.actor.typed.receptionist.{ ServiceKey }

import java.util.{Currency, Locale}

object Guidebook {

  sealed trait Command
  case class Inquiry(countryCode: String, replyTo: ActorRef[Response]) extends Command with CborSerializable

  sealed trait Response
  case class Guidance(countryCode: String, description: String) extends Response with CborSerializable

  val GuidebookServiceKey = ServiceKey[Guidebook.Inquiry]("GuidebookService")

  def apply(): Behavior[Command] = Behaviors.setup(context => new Guidebook(context))
}

import Guidebook._
private class Guidebook(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {

  println(s"Actor ${context.self} spawned and ready to guide tourists!")

  def describe(locale: Locale) =
    s"""In ${locale.getDisplayCountry},
      ${locale.getDisplayCountry} is spoken and the currency
      is the ${Currency.getInstance(locale).getDisplayName}"""

  override def onMessage(msg: Command): Behavior[Command] = {
    println(s"${context.self}: Received a Command: ${msg}")
    msg match {
      case Inquiry(countryCode, replyTo) =>
        println(s"Actor ${context.self} responding to inquiry about $countryCode")
        Locale.getAvailableLocales.
        filter(_.getCountry == countryCode).
        foreach { locale =>
          replyTo ! Guidance(countryCode, describe(locale))
        }
    }
    this
  }
}
