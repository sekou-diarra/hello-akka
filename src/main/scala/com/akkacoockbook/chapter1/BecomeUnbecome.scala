import akka.actor.{Props, ActorSystem, Actor}


class BecomeUnbecomeActor extends Actor {
  def receive: Receive = {
    case true => context.become(isStateTrue)
    case false => context.become(isStateFalse)
    case _ => println("don't know what you want to say !! ")
  }

  def isStateTrue: Receive = {
    case msg:String => println(s"$msg")
    case false => context.become(isStateFalse)
  }

  def isStateFalse:Receive ={
    case msg: Int => println(s"$msg")
    case true => context.become(isStateTrue)
  }
}

object BecomeUnbecomeApp extends App {
  val actorSystem = ActorSystem("Helloakka")
  val becomeUnBecome = actorSystem.actorOf(Props[BecomeUnbecomeActor])

  becomeUnBecome ! true
  becomeUnBecome ! "Hello how are you"
  becomeUnBecome ! false
  becomeUnBecome ! 1100
  becomeUnBecome ! true
  becomeUnBecome ! "What do you do"
}
