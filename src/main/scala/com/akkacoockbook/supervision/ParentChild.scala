package com.akkacoockbook.supervision

import akka.actor._

case object CreateChild

case class Greet(msg: String)

class ChildActor extends Actor {
  override def receive: Receive = {
    case Greet(msg) => println(s"My parent[${self.path.parent}] " +
      s"greeted to me [${self.path}] $msg")
  }
}

class ParentActor extends Actor {
  override def receive: Receive = {
    case CreateChild => val child = context.actorOf(Props[ChildActor], "child")
      child ! Greet("Hello Child")
  }
}

object ParentChild extends App {
  val actorSystem = ActorSystem("supervision")
  val parent = actorSystem.actorOf(Props[ParentActor], "parent")
  parent ! CreateChild
}
