package com.akkacoockbook.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.ActorMaterializer
import scala.concurrent.duration._

import scala.util.Success

object ConnectionLevelAPIApplication extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionCOntext = system.dispatcher

  val connectionFlow = Http().outgoingConnectionHttps("api.github.com")

  val akkaToolkitRequest = HttpRequest(uri = "/repos/akka/akka-http")

  val responseFuture = Source.single(akkaToolkitRequest)
    .via(connectionFlow).runWith(Sink.head)

  responseFuture.andThen {
    case Success(response) =>
      response.entity.toStrict(5 seconds).map(
        _.data.decodeString("UTF-8"))
        .andThen {
          case Success(json) => val pattern = """.*"open_issues":(.*?),.*""".r
            pattern.findAllIn(json).matchData foreach { m =>
              println(s"There are ${m.group(1)} open issues ....")
              materializer.shutdown()
              system.terminate()
            }
          case _ =>
        }
    case _ => println("resquest failed")

  }

}

object HostLevelClientAPIApplication extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val poolClientFlow = Http().cachedHostConnectionPoolHttps[String]("api.github.com")

  val akkaToolkitRequest = HttpRequest(uri = "/repos/akka/akka-http") -> """.*"open_issues":(.*?),.*"""

  val responseFuture = Source.single(akkaToolkitRequest).via(poolClientFlow).runWith(Sink.head)

  responseFuture.andThen {
    case Success(result) =>
      val (tryResponse, regex) = result
      tryResponse match {
        case Success(response) => response.entity.toStrict(5 seconds).map(_.data.decodeString(
          "UTF-8")).andThen {
          case Success(json) => val pattern = regex.r
            pattern.findAllIn(json).matchData foreach {
              m => println(s"There are ${m.group(1)} open issues in Akka Http.")
            }
          case _ =>
        }

        case _ => println("request failed")
      }

    case _ => println("request failed")
  }

}
