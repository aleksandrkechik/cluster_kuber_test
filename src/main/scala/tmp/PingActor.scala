package tmp

import akka.actor.TypedActor.context
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.Config
import tmp.ActorUtils.{Ping1, Pong2}
import tmp.PongActor.props

object PingActor {
  def props = Props(new PingActor())

  def start(config: Config) = {
    val system: ActorSystem = ActorSystem("local-cluster", config)
    // Akka Management hosts the HTTP routes used by bootstrap
    AkkaManagement(system).start()

    // Starting the bootstrap process needs to be done explicitly
    ClusterBootstrap(system).start()
    system.actorOf(props, name = "PingActor")
    val sharding = ClusterSharding(system)
    Cluster(system).registerOnMemberUp({
      system.log.info("Cluster is up!")
    })
    sendPing(system)
  }

//  val extractEntityId: ShardRegion.ExtractEntityId = {
//    case msg@UpdateTemperature(location, _) ⇒ (s"$location", msg)
//    case msg@GetCurrentTemperature(location) ⇒ (s"$location", msg)
//  }

  val numberOfShards = 100

//  val extractShardId: ShardRegion.ExtractShardId = {
//    case UpdateTemperature(location, _) ⇒ (s"$location".hashCode % numberOfShards).toString
//    case GetCurrentTemperature(location) ⇒ (s"$location".hashCode % numberOfShards).toString
//  }

  val shardName = "Ping"

  def sendPing(system: ActorSystem) = {
    val sharding = ClusterSharding(system)
    Thread.sleep(20000)
//    val actor = ClusterSharding(system).start(
//      //    typeName = PongActor.shardName,
//      typeName = "Pong",
//      entityProps = props,
//      settings = ClusterShardingSettings(system),
//      extractEntityId = PongActor.extractEntityId,
//      extractShardId = PongActor.extractShardId)
//    val pongActor = sharding.shardRegion("Pong")
val selection =
  context.actorSelection("akka.tcp://Pong@127.0.0.1:2552/Pong")
    selection ! Ping1()
  }
}
class PingActor() extends Actor{
  override def receive: Receive = {
    case Pong2 =>
      println("GOT PONG!!!!!!")
      sender() ! Ping1()
  }
}

