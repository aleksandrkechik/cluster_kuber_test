package typedPingPong

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.util.Timeout
import typedPingPong.PingPongExample.Guardian

import scala.concurrent.duration.DurationInt


//object Main extends App {
////  val system = ActorSystem(Guardian(), "pingpong")
////  val sharding: ClusterSharding = ClusterSharding(system)
//implicit val timeout: Timeout = Timeout.durationToTimeout(10.seconds)
//  case class Ping()
//
//  def apply(): Behavior[Nothing] = {
//    Behaviors.setup { context =>
//      context.spawn(Guardian(), "pingpong")
//
//      Behaviors.receiveMessage { message =>
//        context.log.info("Got message")
//        context.ask(, context.self)(timeout)
//        Behaviors.same
//      }
//    }
//  }
//}

//object ActorOne {
//  case class InputMsg()
//  case class OutputMsg(from: ActorRef[])
//}
