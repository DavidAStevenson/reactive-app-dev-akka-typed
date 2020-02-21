import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ AbstractBehavior, ActorContext, Behaviors }

import java.util.{Currency, Locale}

object Guidebook {
  def apply(): Behavior[Command] = Behaviors.setup(context => new Guidebook(context))

  sealed trait Command
  case class Inquiry(countryCode: String, replyTo: ActorRef[Response]) extends Command

  sealed trait Response
  case class Guidance(countryCode: String, description: String) extends Response
}

import Guidebook._
private class Guidebook(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {
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
