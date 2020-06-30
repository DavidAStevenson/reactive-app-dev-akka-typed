import akka.NotUsed
import akka.actor.typed.{ ActorSystem, Behavior, SupervisorStrategy }
import akka.actor.typed.scaladsl.{ Behaviors, Routers }
import akka.actor.typed.receptionist.{ Receptionist }
import com.typesafe.config.ConfigFactory

object GuidebookWorld {

  import Guidebook._

  def apply() =
    Behaviors.setup[Nothing] { context =>
      val pool = Routers.pool(poolSize = 3)(
        // make sure the workers are restarted if they fail
        Behaviors.supervise(Guidebook()).onFailure[Exception](SupervisorStrategy.restart)
      )
      val router = context.spawn(pool, "guidebook-pool")
      context.watch(router)

      println(
        s"GuidebookWorld registering ${router} with receptionist, key: ${GuidebookServiceKey}"
      )
      context.system.receptionist ! Receptionist.Register(GuidebookServiceKey, router)

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
    val config = ConfigFactory
      .parseString(s"""
      akka.remote.artery.canonical.port=$port
      """)
      .withFallback(ConfigFactory.load())

    ActorSystem[Nothing](GuidebookWorld(), "TourismWorld", config)
  }
}
