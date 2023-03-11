package fr.andri.kafkastream

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.{KStream, KTable}
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.serialization.Serdes.{longSerde, stringSerde}


import java.time.Duration
import java.util.Properties

object ColourCounter {
  def createTopology: Topology={
    val builder: StreamsBuilder = new StreamsBuilder();
    val textLine: KStream[String, String]= builder.stream[String, String]("color-counter")

    val userAndColor:KStream[String, String] = textLine
      .filter((key, value)=>value.contains(","))
      .selectKey((key, value)=>value.split(",")(0).toLowerCase)
      .mapValues(value=>value.split(",")(1).toLowerCase)
      .filter((user, color)=>List("red", "green", "blue", "purple","white", "black").contains(color))

    userAndColor.to("user-key-and-color")

    val userAndColorTable:KTable[String, String] = builder.table[String, String]("user-key-and-color");

    val favoriteColoursTable: KTable[String,Long] = userAndColorTable
      .groupBy((user: String, colour: String) => (colour, colour))
      .count()

    favoriteColoursTable.toStream.to("user-keys-and-colours-scala")
    builder.build()
  }

  def main(array: Array[String]):Unit={
    val config = new Properties()
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "colour-count-application")
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092, localhost:9093, localhost:9094")
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.getClass)
    config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, stringSerde.getClass)


    val streams = new KafkaStreams(ColourCounter.createTopology, config)
    streams.cleanUp()
    streams.start()

    println(streams)

    sys.addShutdownHook(streams.close(Duration.ofSeconds(5)))
  }

}
