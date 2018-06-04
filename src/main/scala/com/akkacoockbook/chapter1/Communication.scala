package com.akkacoockbook.chapter1

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.util.Random._

class Communication extends Actor {

  import Messages._

  override def receive: Receive = {
    case GiveMeRandomNumber =>
      println("received a message to generate a random integer")
      val randomNumber = nextInt
      sender ! Done(randomNumber)

  }
}

object Communication {

  val props: Props = Props(new Communication)
}

class QueryActor extends Actor {

  import Messages._

  override def receive: Receive = {
    case Start(actorRef) => println(s"send me the next random number")
      actorRef ! GiveMeRandomNumber
    case Done(randomNumber) =>
      println(s"received a random number $randomNumber")
  }
}

object QueryActor{
  def props:Props=Props(new QueryActor)
}

object Messages {

  case class Done(randomNumber: Int)

  case class Start(actorRef: ActorRef)

  case object GiveMeRandomNumber

}

  object Main extends App{
    import Messages._
    val actorSystem =ActorSystem("randomnumbergenerator")
    val randomNumberGenerator = actorSystem.actorOf(Communication.props,"numbergenerator")
    val queryActor = actorSystem.actorOf(QueryActor.props,"queryactor")
    queryActor ! Start(randomNumberGenerator)


}
