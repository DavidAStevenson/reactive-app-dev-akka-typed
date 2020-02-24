import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ AbstractBehavior, ActorContext, Behaviors }

object Tourist {
  // def apply(): Behavior[Start] = Behaviors.setup(context => new Tourist(context))
  def apply(guidebook: ActorRef[Guidebook.Inquiry]): Behavior[Command] =
    Behaviors.setup(context => new Tourist(context, guidebook))

  sealed trait Command
  case class Start(codes: Seq[String]) extends Command
  private case class WrappedInquiryResponse(response: Guidebook.Response) extends Command
}

import Tourist._
private class Tourist(context: ActorContext[Command], guidebook: ActorRef[Guidebook.Inquiry])
  extends AbstractBehavior[Command](context) {

  println(s"Tourist created with ${guidebook} as its Guide...")

  val guidanceResponseAdapter: ActorRef[Guidebook.Response] =
    context.messageAdapter(response => WrappedInquiryResponse(response))

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case Start(codes) =>
        println(s"Received a Start message: ${codes}")
        codes.foreach(guidebook ! Guidebook.Inquiry(_, guidanceResponseAdapter))
      case wrapped: WrappedInquiryResponse =>
        println("Received another type of message...")
        wrapped.response match {
          case Guidebook.Guidance(countryCode, description) =>
            println(s"$countryCode: $description")
          case _ => println("not a Guidance???")
        }
    }
    this
  }
}
