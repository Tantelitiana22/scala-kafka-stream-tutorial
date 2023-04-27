package fr.andri.kafkastream

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.kstream.{JoinWindows, Named, Printed}
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.{Consumed, KStream, Produced, StreamJoined}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import org.apache.kafka.streams.scala.serialization.Serdes

import java.time.Duration
import java.util.Properties

object InnerJoin {
  private def config:Properties={
    val conf:Properties = new Properties
    conf.put(StreamsConfig.APPLICATION_ID_CONFIG, "innerjoin-application")
    conf.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092, localhost:9093, localhost:9094")
    conf.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    conf.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.stringSerde.getClass)
    conf.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.stringSerde.getClass)
    conf
  }

  def topologyBuilder:Topology={
    val  build:StreamsBuilder = new StreamsBuilder

    // [null->001,Andri]
    val ksUser:KStream[String, String] = build.stream("user")(
      Consumed.`with`(Serdes.stringSerde, Serdes.stringSerde))

    // [null->001,Paris]
    val ksAddress = build.stream("address")(
      Consumed.`with`(Serdes.stringSerde,Serdes.stringSerde)
    )

    //change key
    // [001->001,Andri]
    val ksUserWithKey:KStream[String,String] = ksUser.map((_, value)=>(value.split(",")(0),value),
      Named.as("user_info_selectKey"))

    // [001->001,Paris]
    val ksAddressWithKey: KStream[String,String] = ksAddress.map((_,value)=>(value.split(",")(0),value),
      Named.as("user_address_selectkey"))

    //join user and address
    val dataJoined = ksUserWithKey.join(ksAddressWithKey)((left, right)=>left+"---"+right,
      JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(1L)))(
      StreamJoined.`with`(Serdes.stringSerde,Serdes.stringSerde, Serdes.stringSerde)
    ).peek((key, value)=>println(key, value))

    dataJoined.to("inner-join")(Produced.`with`(Serdes.stringSerde,Serdes.stringSerde))
    build.build()

  }
  def main(args:Array[String]):Unit={
    val streams = new KafkaStreams(topologyBuilder, config)
    println(topologyBuilder.describe())
    streams.cleanUp()
    streams.start()

    sys.addShutdownHook(streams.close(Duration.ofSeconds(5)))
  }
}
