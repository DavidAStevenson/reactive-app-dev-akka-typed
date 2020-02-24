import akka.NotUsed
import akka.actor.typed.{ ActorSystem, Behavior }
import akka.actor.typed.scaladsl.{ Behaviors }
import com.typesafe.config.ConfigFactory

object GuidebookWorld {
  def apply(): Behavior[NotUsed] =
    Behaviors.setup { context =>
      val guidebook = context.spawn(Guidebook(), "guidebook")
      context.watch(guidebook)

      Behaviors.empty
    }
}

object GuidebookMain extends App {
  val config = ConfigFactory.parseString(s"""
    akka.remote.artery.canonical.port=25251
    """).withFallback(ConfigFactory.load())

  ActorSystem(GuidebookWorld(), "TourismWorld", config)
}
