package com.akkacoockbook.chapter1

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

class FibonnaciActor extends Actor {

  override def receive: Receive = {
    case num: Int =>
      val fibonnaciNumber = FibonnaciActor.fib(num)
      sender ! fibonnaciNumber
  }

}

object FibonnaciActor {
  def fib(n: Int): Int = n match {
    case 0 | 1 => n
    case _ => fib(n - 1) + fib(n - 2)
  }

  def props: Props = Props(new FibonnaciActor())
}

object Main2 extends App{
  implicit val timeout = Timeout(10 seconds)
  val actorSystem = ActorSystem("FibonaciSystem")
  val actor = actorSystem.actorOf(FibonnaciActor.props)
  val future = (actor ? 10).mapTo[Int]
  val fibonacciNumber = Await.result(future, 10 seconds)
  println(fibonacciNumber)
}
