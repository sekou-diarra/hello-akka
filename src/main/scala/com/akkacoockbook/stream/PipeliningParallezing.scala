package com.akkacoockbook.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, FlowShape}

import scala.util.Random

trait PipeliningParallezing extends App {

  implicit val actorSystem = ActorSystem("PipeliningParallelizing")
  implicit val actorMaterializer = ActorMaterializer()
  val tasks = (1 to 5).map(Wash)
  val parallelStage = Flow.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val dispatchLaundry = builder.add(Balance[Wash](3))
      val mergeLaundry = builder.add(Merge[Done](3))

      dispatchLaundry.out(0) ~> washStage.async ~> dryStage.async ~> mergeLaundry.in(0)
      dispatchLaundry.out(1) ~> washStage.async ~> dryStage.async ~> mergeLaundry.in(1)
      dispatchLaundry.out(2) ~> washStage.async ~> dryStage.async ~> mergeLaundry.in(2)

      FlowShape(dispatchLaundry.in, mergeLaundry.out)
    })

  def washStage = Flow[Wash].map(wash => {
    val sleepTime = Random.nextInt(3) * 1000
    println(s"Washing ${wash.id} It will take $sleepTime milliseconds")
    Thread.sleep(sleepTime)
    Dry(wash.id)
  })

  def dryStage = Flow[Dry].map(dry => {
    val sleepTime = Random.nextInt(3) * 1000
    println(s"Drying ${dry.id}. It will take $sleepTime milliseconds.")
    Thread.sleep(sleepTime)
    Done(dry.id)
  })

  def runGraph(testingFlow: Flow[Wash, Done, NotUsed]) = Source(tasks)
    .via(testingFlow)
    .to(Sink.foreach(println)).run()

  case class Wash(id: Int)

  case class Dry(id: Int)

  case class Done(id: Int)


}

object SynchronousPipeliningApplication extends PipeliningParallezing {

  runGraph(Flow[Wash].via(washStage).via(dryStage))
}

object AsynchronousPipelineApplication extends PipeliningParallezing {
  runGraph(Flow[Wash].via(washStage.async).via(dryStage))
}

object ParallezingApplication extends PipeliningParallezing {
  runGraph(Flow[Wash].via(parallelStage))
}
