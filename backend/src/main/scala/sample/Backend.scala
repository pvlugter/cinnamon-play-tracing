package sample

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.StdIn

object Backend extends App {
  val system = ActorSystem("backend")
  system.actorOf(Props[BackendActor], "backend")
  println(s"Backend started. Press enter to stop...")
  StdIn.readLine()
  system.terminate()
}

class BackendActor extends Actor {
  val worker = context.actorOf(Props[WorkerActor], "worker")

  def receive = {
    case request: String =>
      import context.dispatcher
      val replyTo = sender
      implicit val timeout = Timeout(5.seconds)
      Simulate.delay(min = 10.millis, max = 50.millis)
      (worker ? s"$request + [backend actor]") flatMap {
        case response: String => Database.request(response)
      } pipeTo replyTo
      Simulate.delay(min = 10.millis, max = 50.millis)
  }
}

class WorkerActor extends Actor {
  def receive = {
    case request: String =>
      Simulate.delay(min = 10.millis, max = 100.millis)
      sender ! s"$request + [worker actor]"
  }
}

object Database {
  import com.lightbend.cinnamon.scala.future.named._
  import scala.concurrent.ExecutionContext.Implicits.global

  def request(message: String): Future[String] = {
    FutureNamed("database") {
      Simulate.delay(min = 100.millis, max = 1.second)
      s"$message + [database future]"
    }
  }
}

object Simulate {
  import java.util.concurrent.ThreadLocalRandom.{ current => random }

  def delay(min: Duration, max: Duration): Unit = {
    Thread.sleep(random.nextLong(min.toMillis, max.toMillis))
  }
}
