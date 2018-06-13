package com.akkacoockbook.supervision

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.collection.mutable.ListBuffer
import scala.util.Random


case class DoubleValue(x: Int)

//case object CreateChild
case object Send

case class Response(x: Int)


class DoubleActor extends Actor {
  override def receive: Receive = {
    case DoubleValue(number) => println(s"${self.path.name} Got the $number")
      sender ! Response(number * 2)
  }
}


class ParentActor2 extends Actor {

  val random = new Random
  val childs = ListBuffer[ActorRef]()

  override def receive: Receive = {
    case CreateChild => childs ++= List(context.actorOf(Props[DoubleActor]))
    case Send => println(s"Sending messages to child")
      childs.zipWithIndex foreach { case (child, value) => child ! DoubleValue(random.nextInt(10)) }
    case Response(x) => println(s"Parent: Response from child ${sender.path.name} is $x")
  }
}

object SendMessagesToChild extends App {
  val actorSystem = ActorSystem("Hello-Akka")
  val parent =
    actorSystem.actorOf(Props[ParentActor2], "parent")
  parent ! CreateChild
  parent ! CreateChild
  parent ! CreateChild
  parent ! Send
}
