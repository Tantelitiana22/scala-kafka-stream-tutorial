package fr.andri.bank.serdes

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.kafka.common.errors.SerializationException
object JsonSerializer {
  def serialize(data: JsonNode): Array[Byte] = {
    val objectMapper = new ObjectMapper()
    objectMapper.writeValueAsBytes(data)
  }
}



