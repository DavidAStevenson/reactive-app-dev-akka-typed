import akka.NotUsed
import akka.actor.typed.{ ActorSystem, Behavior }
import akka.actor.typed.scaladsl.{ Behaviors }

import java.util.Locale

object TourismWorld {
  def apply(): Behavior[NotUsed] =
    Behaviors.setup { context =>
      val guidebook = context.spawn(Guidebook(), "guide")
      context.watch(guidebook)
      val tourist = context.spawn(Tourist(guidebook), "dave")
      context.watch(tourist)
      tourist ! Tourist.Start((Locale.getISOCountries).toIndexedSeq)

      Behaviors.empty
    }
}

object Main extends App {
    println("App starting...")

    import Tourist._
    val start = Start(Seq("NZ"))

    import Guidebook._
    val guidance = Guidance("NZ", "New Zealand Dollars are used there")

    ActorSystem(TourismWorld(), "TourismWorld")
}
