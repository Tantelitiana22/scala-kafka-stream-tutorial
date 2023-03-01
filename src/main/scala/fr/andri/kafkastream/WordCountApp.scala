package fr.andri.kafkastream

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.kstream.{KStream, KTable}
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.scala.serialization.Serdes._

import java.time.Duration
import java.util.Properties

object WordCountApp {

  def createTopology : Topology ={
    val builder = new StreamsBuilder()
    val textLines:KStream[String,String] = builder.stream[String, String]("word-count-input")
    val wordCount: KTable[String, Long] = textLines
      .mapValues(textLine=>textLine.toLowerCase)
      .flatMapValues(textLine=>textLine.split("\\W+"))
      .selectKey((key, value)=>value)
      .groupByKey
      .count()

    wordCount.toStream.to("word-count-output")
    builder.build
  }

  def main(array: Array[String]):Unit={
    val config = new Properties()
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-application")
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092, localhost:9093, localhost:9094")
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.stringSerde.getClass)
    config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.stringSerde.getClass)

    val streams = new KafkaStreams(WordCountApp.createTopology, config)
    streams.cleanUp()
    streams.start()

    sys.addShutdownHook(streams.close(Duration.ofSeconds(5)))
  }

}
