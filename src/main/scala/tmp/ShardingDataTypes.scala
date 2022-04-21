package tmp

object ShardingDataTypes {
  case class ShardEnvelope(shardId: Int, entityId: Int, message: Any)

}
