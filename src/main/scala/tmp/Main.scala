package tmp

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.cluster.sharding.ShardRegion
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.{Config, ConfigFactory}
import tmp.ActorUtils.{EntityEnvelope, Get}
object Main2 extends App {
  PongActor.start((ActorUtils.createConfigForPortAndRole(1, 8558, "pong")))
}
object Main extends App{
//  PongActor.start((ActorUtils.createConfigForPortAndRole(1, 8558, "pong")))
  PingActor.start(ActorUtils.createConfigForPortAndRole(2, 8558, "ping"))
//  tmp.PongActor.start((ActorUtils.createConfigForPortAndRole(3333, "pong")))



//  val extractEntityId: ShardRegion.ExtractEntityId = {
//    case EntityEnvelope(id, payload) => (id.toString, payload)
//    case msg @ Get(id)               => (id.toString, msg)
//  }
//
//  val numberOfShards = 100
//
//  val extractShardId: ShardRegion.ExtractShardId = {
//    case EntityEnvelope(id, _)       => (id % numberOfShards).toString
//    case Get(id)                     => (id % numberOfShards).toString
//    case ShardRegion.StartEntity(id) =>
//      // StartEntity is used by remembering entities feature
//      (id.toLong % numberOfShards).toString
//    case _ => throw new IllegalArgumentException()
//  }

}

object Node1 extends App {
  new Main(1)
}

object Node2 extends App {
  new Main(2)
}

object Node3 extends App {
  new Main(3)
}

class Main(nr: Int) {

  val config: Config = ConfigFactory.parseString(s"""
      akka.remote.artery.canonical.hostname = "127.0.0.$nr"
      akka.management.http.hostname = "127.0.0.$nr"
    """).withFallback(ConfigFactory.load())
  val system = ActorSystem("local-cluster", config)

  AkkaManagement(system).start()

  ClusterBootstrap(system).start()

  Cluster(system).registerOnMemberUp({
    system.log.info("Cluster is up!")
  })
}

//class aaa {
//  def receive = {
//    case aaa => println("aaa")
//  }
//
//
//}
