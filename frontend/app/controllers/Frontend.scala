package controllers

import javax.inject._
import play.api.libs.ws._
import play.api.mvc._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import tracedws.TracedWSClient

@Singleton
class Frontend @Inject()(cc: ControllerComponents, ws: WSClient)(implicit executor: ExecutionContext) extends AbstractController(cc) {
  val tws = new TracedWSClient(ws)

  def request(message: String) = Action.async {
    tws.get(s"http://localhost:9001/request/$message") map {
      response => Ok(s"${response.body} + [frontend response]")
    }
  }
}
