package tmp

import com.typesafe.config.ConfigFactory

object ActorUtils {
  case class Ping1()

  case class Pong2()

  final case class Get(counterId: Long)

  final case class EntityEnvelope(id: Long, payload: Any)

  def createConfigForPortAndRole(host: Int, httpPort: Int, role: String) = {
    ConfigFactory.parseString(s"""
      akka.remote.artery.canonical.hostname = "127.0.0.$host"
      akka.management.http.hostname = "127.0.0.$host"
    """).withFallback(ConfigFactory.load("application.conf"))
//    .withFallback(ConfigFactory.parseString(s"akka.remote.netty.tcp.hostname=127.0.0.${host}")).
//            withFallback(ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$httpPort")).
//      withFallback(ConfigFactory.load(s"akka.remote.artery.canonical.port = ${httpPort}"))
//    .withFallback(ConfigFactory.parseString(s"akka.cluster.roles = [${role}]"))
  }
}
