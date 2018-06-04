package com.akkacoockbook.chapter1

import akka.actor.{Actor, Props, ActorSystem}

object BehaviorAndState extends App {

  val actorSystem= ActorSystem ("HelloAkka")
  val actor= actorSystem.actorOf(Props(classOf[SummingActorWithConstructor],10), "summingactor")
  println(actor.path)
  actor ! 1
}


class SummingActorWithConstructor(initialSum: Int) extends Actor {

  var sum = 0

  override def receive: Receive = {

    case x: Int => sum = initialSum + sum + x
      println(s"my state as sum is $sum")

    case _ => println("I don't know what are you talking about")
  }

}
