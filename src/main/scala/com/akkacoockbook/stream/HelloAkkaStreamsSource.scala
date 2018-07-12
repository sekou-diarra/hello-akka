package com.akkacoockbook.stream

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream._

import scala.concurrent.duration._
import akka.stream.stage._

class HelloAkkaStreamsSource extends GraphStage[SourceShape[String]]{

  val out: Outlet[String] = Outlet("SystemInputSource")

  override def shape: SourceShape[String] = SourceShape(out)


  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    setHandler(out,new OutHandler {
      override def onPull(): Unit = {
        val line = "Hello World Akka Streams!"
        push(out,line)
      }
    })
  }
}


class WordCounterSink extends GraphStage[SinkShape[String]] {

val in:Inlet[String] = Inlet("WordCounterSink")


  override def shape: SinkShape[String] = SinkShape(in)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new TimerGraphStageLogic(shape) {
    var counts = Map.empty[String,Int].withDefaultValue(0)

    override def preStart(): Unit = {

      schedulePeriodically(None, 5 seconds)
      pull(in)
    }

    setHandler(in, new InHandler {
      override def onPush(): Unit = {
        val word = grab(in)
        counts += word -> (counts(word) +1)
        pull(in)
      }
    })

    override def onTimer(timerKey: Any) =
      println(s"At ${System.currentTimeMillis()} count map is $counts")
  }
}



object CustomStageApplication extends App {

  implicit val actorSystem = ActorSystem("CustomStages")
  implicit val actorMaterializer = ActorMaterializer()

  val source = Source.fromGraph(new HelloAkkaStreamsSource())
  val upperCaseMapper = Flow[String].map(_.toUpperCase)
  val splitter = Flow[String].mapConcat(_.split(" ").toList)
  val punctuationMapper = Flow[String].map(_.replaceAll("""
										[p{Punct}&&[^.]]""",	"").replaceAll(
    System.lineSeparator(),	""))
  val	filterEmptyElements	=	Flow[String].filter(_.nonEmpty)
  val	wordCounterSink	=	Sink.fromGraph(new	WordCounterSink())

  val	stream	=	source
    .via(upperCaseMapper)
    .via(splitter)
    .via(punctuationMapper)
    .via(filterEmptyElements)
    .to(wordCounterSink)
  stream.run()
}
