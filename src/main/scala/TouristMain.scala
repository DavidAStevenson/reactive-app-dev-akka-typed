import akka.actor.typed.{ ActorSystem, Behavior }
import akka.actor.typed.scaladsl.{ Behaviors }
import akka.actor.typed.receptionist.Receptionist
import com.typesafe.config.ConfigFactory

import java.util.Locale

object TourismWorld {

  sealed trait Command
  private case class ListingResponse(listing: Receptionist.Listing) extends Command

  //def apply(): Behavior[Command] =
  //  Behaviors.setup[Command] { context =>
  def apply(): Behavior[Nothing] =
    Behaviors.setup[Receptionist.Listing] { context =>
      //val listingResponseAdapter = context.messageAdapter[Receptionist.Listing](ListingResponse)

      // context.system.receptionist ! Receptionist.Find(Guidebook.GuidebookServiceKey, listingResponseAdapter)
      // println(s"Trying to find Guidebook with ${Guidebook.GuidebookServiceKey}")
      context.system.receptionist ! Receptionist.Subscribe(Guidebook.GuidebookServiceKey, context.self)
      println(s"Subscribing to find Guidebook with ${Guidebook.GuidebookServiceKey}")

      Behaviors.receiveMessagePartial[Receptionist.Listing] {
        case Guidebook.GuidebookServiceKey.Listing(listings) =>
          listings.foreach { guidebook =>
              //context.spawn(Tourist(guidebook), "dave")
            println(s"Spawning tourist with ${guidebook}")
            val tourist = context.spawn(Tourist(guidebook), "dave")
            tourist ! Tourist.Start((Locale.getISOCountries).toIndexedSeq)
          }
          Behaviors.same
      }

      /*
      Behaviors.receiveMessage {
        case ListingResponse(Guidebook.GuidebookServiceKey.Listing(listings)) =>
          listings.foreach { guidebook => 
            println(s"Spawning tourist with ${guidebook}")
            val tourist = context.spawn(Tourist(guidebook), "dave")
            tourist ! Tourist.Start((Locale.getISOCountries).toIndexedSeq)
          }
          Behaviors.same
      }
      */
    }.narrow

}

object TouristMain extends App {
  val config = ConfigFactory.parseString(s"""
    akka.remote.artery.canonical.port=25252
    """).withFallback(ConfigFactory.load())

  ActorSystem[Nothing](TourismWorld(), "TourismWorld", config)
}
