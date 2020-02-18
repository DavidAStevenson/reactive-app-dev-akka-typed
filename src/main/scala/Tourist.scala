import akka.actor.typed.{ Behavior }
import akka.actor.typed.scaladsl.{ AbstractBehavior, ActorContext }

object Tourist {
  sealed trait Command
  case class Start(codes: Seq[String]) extends Command
}

import Tourist.Start
class Tourist(context: ActorContext[Start]) extends AbstractBehavior[Start](context) {

  override def onMessage(msg: Start): Behavior[Start] = {
    println("Received a Start message")
    this
  }
}
