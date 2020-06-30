import akka.actor.typed.{ ActorSystem, Behavior }
import akka.actor.typed.scaladsl.{ Behaviors, Routers }
import akka.actor.typed.receptionist.Receptionist
import com.typesafe.config.ConfigFactory

import java.util.Locale

object TourismWorld {

  def apply() =
    Behaviors
      .setup[Nothing] { context =>
        val group = Routers.group(Guidebook.GuidebookServiceKey)
        val router = context.spawn(group, "guidebook-group")

        val tourist = context.spawn(Tourist(router), s"tourist")
        Thread.sleep(5000) // YUCK!
        tourist ! Tourist.Start((Locale.getISOCountries).toIndexedSeq)

        Behaviors.same
      }
      .narrow

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
    val config = ConfigFactory
      .parseString(s"""
      akka.remote.artery.canonical.port=$port
      """)
      .withFallback(ConfigFactory.load())

    ActorSystem[Nothing](TourismWorld(), "TourismWorld", config)
  }
}
