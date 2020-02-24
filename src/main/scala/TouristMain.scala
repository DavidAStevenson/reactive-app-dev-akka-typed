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
          listings.foreach { guidebook =>
            println(s"Spawning tourist with ${guidebook}")
            val tourist = context.spawn(Tourist(guidebook), "dave")
            tourist ! Tourist.Start((Locale.getISOCountries).toIndexedSeq)
          }
          Behaviors.same
      }
    }.narrow

}

object TouristMain extends App {
  val config = ConfigFactory.parseString(s"""
    akka.remote.artery.canonical.port=25252
    """).withFallback(ConfigFactory.load())

  ActorSystem[Nothing](TourismWorld(), "TourismWorld", config)
}
