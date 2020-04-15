import akka.actor.typed.{ ActorSystem, Behavior }
import akka.actor.typed.scaladsl.{ Behaviors }
import akka.actor.typed.receptionist.Receptionist
import com.typesafe.config.ConfigFactory

import java.util.Locale

object TourismWorld {

  sealed trait Command
  private case class ListingResponse(listing: Receptionist.Listing) extends Command

  def apply(): Behavior[Nothing] =
    Behaviors.setup[Receptionist.Listing] { context =>

      context.system.receptionist ! Receptionist.Subscribe(Guidebook.GuidebookServiceKey, context.self)
      println(s"Subscribing to find Guidebook with ${Guidebook.GuidebookServiceKey}")

      Behaviors.receiveMessagePartial[Receptionist.Listing] {
        case Guidebook.GuidebookServiceKey.Listing(listings) =>
          var count = 0
          listings.foreach { guidebook =>
            println(s"Spawning tourist with ${guidebook}")
            val tourist = context.spawn(Tourist(guidebook), s"tourist-$count")
            count += 1
            tourist ! Tourist.Start((Locale.getISOCountries).toIndexedSeq)
          }
          Behaviors.same
      }
    }.narrow

}

object TouristMain {

  def main(args: Array[String]): Unit = {
    val port =
      if (args.isEmpty)
        25252
      else
        args(0).toInt
    startup(port)
  }

  def startup(port: Int): Unit = {
    val config = ConfigFactory.parseString(s"""
      akka.remote.artery.canonical.port=$port
      """).withFallback(ConfigFactory.load())

    ActorSystem[Nothing](TourismWorld(), "TourismWorld", config)
  }
}
