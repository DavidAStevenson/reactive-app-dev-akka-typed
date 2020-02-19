import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ AbstractBehavior, ActorContext, Behaviors }

object Tourist {
  // def apply(): Behavior[Start] = Behaviors.setup(context => new Tourist(context))
  def apply(guidebook: ActorRef[Guidebook.Command]): Behavior[Start] =
    Behaviors.setup(context => new Tourist(context, guidebook))

  sealed trait Command
  case class Start(codes: Seq[String]) extends Command
}

import Tourist.Start
private class Tourist(context: ActorContext[Start], guidebook: ActorRef[Guidebook.Command])
  extends AbstractBehavior[Start](context) {

  override def onMessage(msg: Start): Behavior[Start] = {
    println(s"Received a Start message: ${msg}")
    guidebook ! Guidebook.Inquiry(msg.codes(0))
    this
  }
}
