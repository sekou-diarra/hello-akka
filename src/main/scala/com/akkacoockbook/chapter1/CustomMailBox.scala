package com.akkacoockbook.chapter1

import java.util.concurrent.ConcurrentLinkedQueue

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.dispatch.{Envelope, MailboxType, MessageQueue, ProducesMessageQueue}
import com.typesafe.config.Config

class MyMessageQueue extends MessageQueue {
  private final val queue = new ConcurrentLinkedQueue[Envelope]()

  override def enqueue(receiver: ActorRef, handle: Envelope): Unit = {
    if (handle.sender.path.name == "MyActor") {
      handle.sender ! "Het dude, How are you?, I know your name, processing your request"
      queue.offer(handle)
    } else handle.sender ! "I don't talk to strangers, I can't process your request"
  }

  override def numberOfMessages: Int = queue.size()

  override def cleanUp(owner: ActorRef, deadLetters: MessageQueue) {
    while (hasMessages) {
      deadLetters.enqueue(owner, dequeue())
    }
  }

  override def dequeue(): Envelope = queue.poll()

  override def hasMessages: Boolean = !queue.isEmpty
}

class MyUnboundedMailBox extends MailboxType with ProducesMessageQueue[MyMessageQueue] {
  def this(settings: ActorSystem.Settings, config: Config) = {
    this()
  }

  override def create(owner: Option[ActorRef], system: Option[ActorSystem]): MessageQueue = new MyMessageQueue()
}

class MySpecialActor extends Actor {
  override def receive:Receive ={
    case msg: String=> println(s"msg is $msg")
  }
}
class MyActor extends Actor {
  override def receive: Receive = {
    case (msg: String, actorRef: ActorRef) => actorRef !
      msg
    case msg => println(msg)
  }
}

object MyActor{
  def props:Props=Props(new MyActor)
}


object MySpecialActor{
  def props:Props=Props(new MySpecialActor)
}

object CustomMailBox extends App {
  val actorSystem = ActorSystem("mailbox")
  val actor = actorSystem
    .actorOf(MySpecialActor.props.withDispatcher("custom-dispatcher"))
  val actor1 = actorSystem.actorOf(MyActor.props,"xyz")
  val actor2 = actorSystem.actorOf(MyActor.props, "MyActor")
  actor1 ! ("hello", actor)
  actor2 ! ("hello", actor)
}
