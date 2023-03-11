package fr.andri.bank.serdes

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.kafka.common.errors.SerializationException

object JsonDeserializer {
  def deserialize(bytes: Array[Byte]): JsonNode = {
    val objectMapper: ObjectMapper = new ObjectMapper();
    objectMapper.readTree(bytes)
  }
}
