package fr.andri.bank

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.kafka.clients.producer.ProducerRecord
import org.scalatest.PrivateMethodTester
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class bankTransactionProducerTest extends AnyFunSuite with Matchers with PrivateMethodTester {
  test("bank transaction producer create random data"){
      val privateMethod= PrivateMethod[ProducerRecord[String,String]](Symbol("newRandomTransaction"))
      val record: ProducerRecord[String,String] = BankTransactionProducer invokePrivate privateMethod("andri")
      val key: String = record.key()
      val value: String = record.value()

      val mapper: ObjectMapper = new ObjectMapper()
      try {
        val node: JsonNode = mapper.readTree(value)
        node.get("name").asText should equal("andri")
        node.get("amount").asInt should be < 100
      }catch {
        case e: Exception=>e.printStackTrace()
      }
      key should equal("andri")
    }
}


