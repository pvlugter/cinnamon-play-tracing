package controllers

import javax.inject._
import play.api.libs.ws._
import play.api.mvc._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class Frontend @Inject()(cc: ControllerComponents, ws: WSClient)(implicit executor: ExecutionContext) extends AbstractController(cc) {
  def request(message: String) = Action.async {
    ws.url(s"http://localhost:9001/request/$message").get map {
      response => Ok(s"${response.body} + [frontend response]")
    }
  }
}
