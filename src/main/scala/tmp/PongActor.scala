package tmp

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.Config
import tmp.ActorUtils.{Ping1, Pong2}
import tmp.PingActor.props

object PongActor {
  def props = Props(new PongActor())

  def start(config: Config) = {
    val system = ActorSystem("local-cluster", config)
    // Akka Management hosts the HTTP routes used by bootstrap
    AkkaManagement(system).start()

    // Starting the bootstrap process needs to be done explicitly
    ClusterBootstrap(system).start()
//    system.actorOf(props, name = "PongActor")

    val actor = ClusterSharding(system).start(
      //    typeName = PongActor.shardName,
      typeName = "Pong",
      entityProps = props,
      settings = ClusterShardingSettings(system),
      extractEntityId = PongActor.extractEntityId,
      extractShardId = PongActor.extractShardId)


    Cluster(system).registerOnMemberUp({
      system.log.info("Cluster is up!")
    })
  }

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case msg@Ping1() ⇒ (s"Ping", msg)
  }

  val numberOfShards = 100

  val extractShardId: ShardRegion.ExtractShardId = {
    case Ping1() ⇒
      println("GOT PING!!!!!!!!!!!!!!")
//      sender() ! Pong()
      println((s"Ping".hashCode % numberOfShards).toString)
      (s"Ping".hashCode % numberOfShards).toString

  }

  val shardName = "Pong"
}
class PongActor extends Actor {
  override def receive: Receive = {
    case Ping1 =>
      println("GOT PING!!!!!!!!!!!!!!")
      sender() ! Pong2()
  }
}
