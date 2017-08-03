package controllers

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import javax.inject._
import play.api.mvc._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class Service @Inject()(cc: ControllerComponents, actorSystem: ActorSystem)(implicit executor: ExecutionContext) extends AbstractController(cc) {
  // direct remote actor selection just for this sample — should be clustered actor in real app
  val backend = actorSystem.actorSelection("akka://backend@127.0.0.1:25521/user/backend")

  def request(message: String) = Action.async {
    implicit val timeout = Timeout(5.seconds)
    (backend ? message).mapTo[String] map {
      response => Ok(s"$response + [service response]")
    }
  }
}
