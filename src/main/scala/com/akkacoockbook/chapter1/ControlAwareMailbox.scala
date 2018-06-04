import akka.dispatch.ControlMessage
import akka.actor.{Props, Actor, ActorSystem}

case object MyControlMessage extends ControlMessage

class Logger extends Actor {
  override def receive: Receive = {
    case MyControlMessage => println("Oh, I have to process Control message first")
    case x => println(x.toString)
  }
}


object ControlAwareMailbox extends App {

  val actorSystem = ActorSystem("Helloakka")
  val actor = actorSystem.actorOf(Props[Logger].withDispatcher(
    "control-aware-dispatcher"
  ))
  actor ! "hello"
  actor ! "how are"
  actor ! "you?"
  actor ! MyControlMessage
}
