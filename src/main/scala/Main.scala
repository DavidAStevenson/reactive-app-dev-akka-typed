import akka.NotUsed
import akka.actor.typed.{ ActorSystem, Behavior }
import akka.actor.typed.scaladsl.{ Behaviors }


object TourismWorld {
  def apply(): Behavior[NotUsed] =
    Behaviors.setup { context =>
      val guidebook = context.spawn(Guidebook(), "guide")
      context.watch(guidebook)
      val tourist = context.spawn(Tourist(guidebook), "dave")
      context.watch(tourist)
      tourist ! Tourist.Start(Seq("NZ"))

      Behaviors.empty
    }
}

object Main extends App {
    println("App starting...")

    import Tourist._
    val start = Start(Seq("NZ"))

    import Guidebook._
    //val inquiry = Inquiry("NZ")
    val guidance = Guidance("NZ", "New Zealand Dollars are used there")

    ActorSystem(TourismWorld(), "TourismWorld")
}
