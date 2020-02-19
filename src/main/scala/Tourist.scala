import akka.actor.typed.{ Behavior }
import akka.actor.typed.scaladsl.{ AbstractBehavior, ActorContext, Behaviors }

object Tourist {
  def apply(): Behavior[Start] = Behaviors.setup(context => new Tourist(context))

  sealed trait Command
  case class Start(codes: Seq[String]) extends Command
}

import Tourist.Start
class Tourist(context: ActorContext[Start]) extends AbstractBehavior[Start](context) {

  override def onMessage(msg: Start): Behavior[Start] = {
    println(s"Received a Start message: ${msg}")
    this
  }
}
