package fr.andri.bank

import fr.andri.bank.serdes.JsonSerde.jsonSerde
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.databind.node.{JsonNodeFactory, ObjectNode}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.scala.{ByteArrayKeyValueStore, StreamsBuilder}
import org.apache.kafka.streams.scala.kstream.{KStream, KTable, Materialized}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import org.apache.kafka.streams.scala.serialization.Serdes.stringSerde
import org.apache.kafka.streams.scala.ImplicitConversions._

import java.time.{Duration, Instant}
import java.util.Properties

object BankBalanceExactlyOnceApp {
  private def properties: Properties={
    val conf:Properties = new Properties()
    conf.put(StreamsConfig.APPLICATION_ID_CONFIG, "bank-balance-application")
    conf.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092, localhost:9093, localhost:9094")
    conf.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest")

    conf.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_DOC, "0")
    conf.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2)
    conf
  }

  def createTopology: Topology={

    val initialBalance: ObjectNode = JsonNodeFactory.instance.objectNode()
    initialBalance.put("count", 0)
    initialBalance.put("balance", 0)
    initialBalance.put("time", Instant.ofEpochMilli(0L).toString)
    val initialJsonNode: JsonNode = new ObjectMapper().readTree(initialBalance.toString)


    implicit val materialized: Materialized[String, JsonNode, ByteArrayKeyValueStore] = {
      Materialized.as("aggregated")(stringSerde, jsonSerde)
    }

    val builder: StreamsBuilder = new StreamsBuilder()
    val bankTransaction: KStream[String, JsonNode] = builder.stream[String, JsonNode]("bank-transactions")

    val bankBalance:KTable[String, JsonNode] = bankTransaction
      .groupByKey
      .aggregate(initialJsonNode)((_,transaction, balance)=>newBalance(transaction, balance))(materialized)

    bankBalance.toStream.to("bank-balance-exactly-once")

    builder.build()
  }

  private def newBalance(transaction:JsonNode, balance: JsonNode):JsonNode={
      val balanceNew: ObjectNode = JsonNodeFactory.instance.objectNode()
      balanceNew.put("count",balance.get("count").asInt+1)
      balanceNew.put("balance", balance.get("balance").asInt+transaction.get("amount").asInt)

      val balanceEpoch:Long = Instant.parse(balance.get("time").asText).toEpochMilli
      val transactionEpoch:Long = Instant.parse(transaction.get("time").asText).toEpochMilli
      val newBalanceInstant: Instant = Instant.ofEpochMilli(Math.max(balanceEpoch,transactionEpoch))
      balanceNew.put("time", newBalanceInstant.toString)
      balanceNew
  }

  def main(array:Array[String]):Unit={
    val streams: KafkaStreams = new KafkaStreams(createTopology, properties)
    streams.cleanUp()
    streams.start()
    sys.addShutdownHook(streams.close(Duration.ofSeconds(5)))
  }

}
