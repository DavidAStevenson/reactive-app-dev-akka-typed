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

object GuidebookMain {

  def main(args: Array[String]): Unit = {
    val port =
      if (args.isEmpty)
        25251
      else
        args(0).toInt
    startup(port)
  }

  def startup(port: Int): Unit = {
    val config = ConfigFactory.parseString(s"""
      akka.remote.artery.canonical.port=$port
      """).withFallback(ConfigFactory.load())

    ActorSystem(GuidebookWorld(), "TourismWorld", config)
  }
}
