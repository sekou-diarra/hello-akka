package com.akkacoockbook.stream

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{SourceQueueWithComplete}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import com.akkacoockbook.stream.SinkActor.{AckSinkActor, CompletedSinkActor, InitSinkActor}
import akka.pattern.ask


import scala.concurrent.duration._

class StringCleanerActor extends Actor {
  override def receive: Receive = {
    case s: String => println(s"Cleaning [$s] in StringCleaner")
      sender ! s.replaceAll(
        """[p{Punct}&&[^.]]""",
        "").replaceAll(System.lineSeparator(), "")
  }
}


object SinkActor {

  case object CompletedSinkActor

  case object AckSinkActor

  case object InitSinkActor

}

class SinkActor extends Actor {
  override def receive: Receive = {
    case InitSinkActor => println("SinkActor initialised")
      sender ! AckSinkActor

    case something => println(s"Received [$something] in SinkActor")
      sender ! AckSinkActor
  }
}

object SourceActor {
  def props(sourceQueue: SourceQueueWithComplete[String]) =
    Props(new SourceActor(sourceQueue))

  case object Tick

}

class SourceActor(sourceQueue: SourceQueueWithComplete[String]) extends Actor {

  import context.dispatcher
  import SourceActor._

  override def preStart(): Unit = {
    context.system.scheduler.schedule(0 seconds, 5 seconds, self, Tick)
  }

  override def receive = {
    case Tick => println(s"Offering element from SourceActor")
      sourceQueue.offer("Integrating!!### Akka$$$ Actors? with}{ Akka** Streams")
  }
}

object IntegratingWithActorsApplication extends App {
  implicit val actorSystem = ActorSystem("IntegratingWithActors")
  implicit val actorMaterializer = ActorMaterializer()

  implicit val askTimeout = Timeout(5 seconds)
  val stringCleaner = actorSystem.actorOf(Props[StringCleanerActor])

  val sinkActor = actorSystem.actorOf(Props[SinkActor])

  val source = Source.queue[String](100, OverflowStrategy.backpressure)

  val sink = Sink.actorRefWithAck[String](sinkActor, InitSinkActor, AckSinkActor, CompletedSinkActor)

  val queue: SourceQueueWithComplete[String] = source
    .mapAsync(parallelism = 2)(elem => (stringCleaner ? elem).mapTo[String])
    .to(sink)
    .run()
  actorSystem.actorOf(SourceActor.props(queue))

}
