package com.akkacoockbook.stream

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.akkacoockbook.stream.WorkingWithGraphsApplication.GenericMsg

import scala.concurrent.duration._
import scala.util.Random

class StatefulCounterFlow extends GraphStage[FlowShape[Seq[GenericMsg], Int]] {

  val in: Inlet[Seq[GenericMsg]] = Inlet("IncomingGenericMsg")

  val out: Outlet[Int] = Outlet("OutgoingCount")

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      var count = 0

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val elem = grab(in)
          count += elem.size
          push(out, count)

        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = pull(in)
      })
    }

  override def shape: FlowShape[Seq[GenericMsg], Int] = FlowShape(in, out)

}

object WorkingWithGraphsApplication extends App {
  implicit val actorSystem = ActorSystem("WorkingWithGraphs")
  implicit val actorMaterializer = ActorMaterializer()
  val graph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      //source
      val androidNotification = Source.tick(
        2 seconds, 500 millis, new AndroidMsg()
      )

      val iOSnotification = Source.tick(700 millis,
        600 millis, new IosMsg)

      //Flow
      val groupAndroid = Flow[AndroidMsg]
        .map(_.toGenMesg("ANDROID"))
        .groupedWithin(5, 5 seconds).async

      val groupIos = Flow[IosMsg]
        .map(_.toGenMesg("IOS"))
        .groupedWithin(5, 5 seconds).async

      def counter = Flow[Seq[GenericMsg]].via(new StatefulCounterFlow())

      val mapper = Flow[Seq[GenericMsg]].mapConcat(_.toList)

      //Junctions
      val aBroadcast = builder.add(Broadcast[Seq[GenericMsg]](2))
      val iBroadcast = builder.add(Broadcast[Seq[GenericMsg]](2))
      val balancer = builder.add(Balance[Seq[GenericMsg]](2))
      val notificationMerge = builder.add(Merge[Seq[GenericMsg]](2))
      val genericNotificationMerge = builder.add(Merge[GenericMsg](2))

      //Sink
      def counterSink(s: String) =
        Sink.foreach[Int](x => println(s"$s: [$x]"))

      //Graph
      androidNotification ~> groupAndroid ~> aBroadcast ~>
        counter ~> counterSink("Android")
      aBroadcast ~> notificationMerge
      iBroadcast ~> notificationMerge
      iOSnotification ~> groupIos ~> iBroadcast ~> counter ~> counterSink("Ios")

      notificationMerge ~> balancer ~> mapper.async ~> genericNotificationMerge

      balancer ~> mapper.async ~> genericNotificationMerge

      genericNotificationMerge ~> Sink.foreach(println)

      ClosedShape
    }
  )

  trait MobileMsg {
    def toGenMesg(origin: String) = GenericMsg(id, origin)

    def id = Random.nextInt(1000)
  }

  class AndroidMsg extends MobileMsg

  class IosMsg extends MobileMsg

  case class GenericMsg(i: Int, str: String)

  graph.run()
}
