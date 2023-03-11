package fr.andri.bank.serdes

import com.fasterxml.jackson.databind.JsonNode
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.scala.serialization.Serdes

object JsonSerde {
  private val deserializer = (bytes: Array[Byte])=> Some(JsonDeserializer.deserialize(bytes))
  private val serializer = (a:JsonNode)=>JsonSerializer.serialize(a)
  implicit val jsonSerde:Serde[JsonNode] = Serdes.fromFn[JsonNode](serializer, deserializer)
}
