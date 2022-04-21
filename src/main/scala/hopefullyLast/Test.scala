package hopefullyLast

import akka.actor.typed.receptionist.Receptionist.{Find, Listing}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.Cluster
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.remote.transport.ActorTransportAdapter.AskTimeout
import com.typesafe.config.{Config, ConfigFactory}
import hopefullyLast.ClusterManager.Command
import hopefullyLast.OtherActor.otherActorKey
import hopefullyLast.SomeActor.{IncomingMessage, Response, someActorKey}

import scala.util.{Failure, Success}

object ClusterManager {
  sealed trait Command
//  case object PingAll extends Command
  private case class ListingResponse(listing: Receptionist.Listing) extends Command
  private case class MessageForwarding(listing: Receptionist.Listing, msg: Any) extends Command
  private case class FoundSomeActor(listing: Listing, msg: IncomingMessage) extends Command
  private case class FoundOtherActor(listing: Listing, msg: Response) extends Command
  private case class ActorNotFound() extends Command
  private case class ActorFoundYohoo() extends Command


  def apply(): Behavior[Command] = {
    Behaviors.setup[Command] { context: ActorContext[Command] =>
      val listingResponseAdapter = context.messageAdapter[Receptionist.Listing](ListingResponse.apply)

      Behaviors.receiveMessagePartial {
        case msg: IncomingMessage =>
          println("Incoming message have been sent to Manager")
          findRefAndForwardMessage(context, someActorKey, msg)
          Behaviors.same
        case response: Response =>
          println("Response have been sent to Manager")
          findRefAndForwardMessage(context, otherActorKey, response)
          Behaviors.same
      }
    }
  }
  def findRefAndForwardMessage[A](context: ActorContext[Command], key: ServiceKey[A], msg: A) = {
    context.ask(
      context.system.receptionist,
      Find(key)
    ) {
      case Success(listing: Listing) =>
        val instances: Set[ActorRef[A]] = {
          listing.serviceInstances(key)
        }
//        instances match {
//          case refs =>
//            refs.foreach { m =>
//              m ! msg
//            }
//          case Set.empty =>
//            println("Couldn't find actor for key")
//        }
        instances.foreach { m =>
          m ! msg
        }
        ActorFoundYohoo()
      case Failure(_) =>
        println("Couldn't find actor for key")
        ActorNotFound()
    }
  }

}

sealed trait MySystemKeySet
sealed trait MySystem
object SomeActor extends MySystem {
  val someActorKey = ServiceKey[IncomingMessage]("someActor")
  case class IncomingMessage(msg: String) extends Command
  case class Response(msg: String) extends Command
  def apply(manager: ActorRef[Command]): Behavior[IncomingMessage] =
  Behaviors.setup{ context: ActorContext[IncomingMessage] =>
    context.system.receptionist ! Receptionist.Register(someActorKey, context.self)
    Behaviors.receive { (context, message) =>
      println(message.msg)
      manager ! Response("I've got your message")
      Behaviors.same
    }
  }
}

object OtherActor extends MySystem {
  val otherActorKey: ServiceKey[Response] = ServiceKey[Response]("otherActor")
  def apply(): Behavior[Response] =
    Behaviors.setup { context =>
      context.system.receptionist ! Receptionist.Register(otherActorKey, context.self)

      Behaviors.receive {
        case (context, message) =>
          println(s"I've got response!!! ${message.msg}")
          Behaviors.same
      }
    }

//  def sendMessage = {
//    Behaviors.setup((context => SomeActor ! IncomingMessage("Yo!", ActorRef[OtherActor]))
//  }
}
//class OtherActor(context: ActorContext[SomeActor.Response]) extends AbstractBehavior[SomeActor.Response](context) {
////  override
////  def onMessage(msg: SomeActor.IncomingMessage): Behavior[SomeActor.IncomingMessage] = {
////    println(s"I've got response!!! ${msg.msg}")
////    Behaviors.same
////  }
//
//  override def onMessage(msg: Response): Behavior[Response] = {
//    println(s"I've got response!!! ${msg.msg}")
//    Behaviors.same
//  }
//}

object Main extends App {
  final case class Start(clientName: String)

  def apply(): Behavior[Nothing] =
    Behaviors.setup[Receptionist.Listing] { context: ActorContext[Receptionist.Listing] =>
      context.log.info("Tuning environment")
//      val bank = context.spawn(Bank(), "bank")
      val manager: ActorRef[Command] = context.spawn(ClusterManager(), "manager")

//      val someActor: ActorRef[IncomingMessage] = context.spawn(SomeActor(manager), "someActor")
      val otherActor: ActorRef[Response] = context.spawn(OtherActor(), "otherActor")

//      context.system.receptionist ! Receptionist.Subscribe(someActorKey, context.self)
      context.system.receptionist ! Receptionist.Subscribe(otherActorKey, context.self)
//      Thread.sleep(10000)
      manager ! IncomingMessage("aaa")

      Behaviors.receiveMessage { message =>
        context.log.info("Starting...")
//        someActor ! IncomingMessage("The message", otherActor)
        Behaviors.same
      }
    }.narrow

  val config: Config = ConfigFactory.parseString(s"""
      akka.remote.artery.canonical.hostname = "127.0.0.2"
      akka.management.http.hostname = "127.0.0.2"
    """).withFallback(ConfigFactory.load())
  val system = ActorSystem[Nothing](Main(), "Test", config)
  AkkaManagement(system).start()

  ClusterBootstrap(system).start()

  Cluster(system).registerOnMemberUp({
    system.log.info("Cluster is up!")
  })
//  system ! Start("Start")
}

object Main2 extends App {
  final case class Start(clientName: String)

  def apply(): Behavior[Nothing] =
    Behaviors.setup[Receptionist.Listing] { context: ActorContext[Receptionist.Listing] =>
      context.log.info("Tuning environment")
      //      val bank = context.spawn(Bank(), "bank")
      val manager: ActorRef[Command] = context.spawn(ClusterManager(), "manager")

      val someActor: ActorRef[IncomingMessage] = context.spawn(SomeActor(manager), "someActor")
//      val otherActor: ActorRef[Response] = context.spawn(OtherActor(), "otherActor")

      context.system.receptionist ! Receptionist.Subscribe(someActorKey, context.self)
//      context.system.receptionist ! Receptionist.Subscribe(otherActorKey, context.self)
//      Thread.sleep(10000)
//      manager ! IncomingMessage("aaa")

      Behaviors.receiveMessage { message =>
        context.log.info("Starting...")
        //        someActor ! IncomingMessage("The message", otherActor)
        Behaviors.same
      }
    }.narrow

  val config: Config = ConfigFactory.parseString(s"""
      akka.remote.artery.canonical.hostname = "127.0.0.1"
      akka.management.http.hostname = "127.0.0.1"
    """).withFallback(ConfigFactory.load())
  val system = ActorSystem[Nothing](Main2(), "Test", config)
  AkkaManagement(system).start()

  ClusterBootstrap(system).start()

  Cluster(system).registerOnMemberUp({
    system.log.info("Cluster is up!")
  })
  //  system ! Start("Start")
}

object Temp extends App {

}
