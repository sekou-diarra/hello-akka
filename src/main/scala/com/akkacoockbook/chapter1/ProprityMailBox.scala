package com.akkacoockbook.chapter1

import akka.actor.{Actor, ActorSystem, Props}
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config

class PriorityMailActor extends Actor {
  override def receive: Receive = {
    //    Int Messages
    case x: Int => println(x)
    //    String Messages
    case x: String => println(x)
    //    Long Messages
    case x: Long => println(x)
    //    other Messages
    case x => println(x)

  }
}

object PriorityMailActor{

  def props =Props(new PriorityMailActor)
}

class MyPriorityActorMailBox(settings: ActorSystem.Settings, config: Config)
  extends UnboundedPriorityMailbox(
    PriorityGenerator {
      //    Int Messages
      case x: Int => 1
      //    String Messages
      case x: String => 0
      //    Long Messages
      case x: Long => 2
      //    other Messages
      case _ =>3
    }
  )

object PriorityMailBoxApp extends App {
  val actorSystem =ActorSystem("helloAkka")
  val priorityActor = actorSystem.actorOf(PriorityMailActor.props.withDispatcher("prio-dispatcher"))
  priorityActor ! 6.0
  priorityActor ! 1
  priorityActor ! 5.0
  priorityActor ! 3
  priorityActor ! "Hello"
  priorityActor ! 5
  priorityActor ! "I am priority actor"
  priorityActor ! "I process string messages first,then integer, long and others"
}
