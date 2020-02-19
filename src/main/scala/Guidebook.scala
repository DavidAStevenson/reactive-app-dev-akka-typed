import akka.actor.typed.{ Behavior }
import akka.actor.typed.scaladsl.{ AbstractBehavior, ActorContext, Behaviors }

object Guidebook {
  def apply(): Behavior[Command] = Behaviors.setup(context => new Guidebook(context))

  sealed trait Command
  case class Inquiry(countryCode: String) extends Command

  sealed trait Response
  case class Guidance(countryCode: String, description: String) extends Response
}

import Guidebook.Command
private class Guidebook(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {
  override def onMessage(msg: Command): Behavior[Command] = {
    println(s"${context.self}: Received a Command: ${msg}")
    this
  }
}
