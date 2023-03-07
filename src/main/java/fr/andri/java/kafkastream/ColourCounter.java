package fr.andri.java.kafkastream;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Named;

import java.util.Arrays;
import java.util.Properties;

public class ColourCounter {

    private static Properties getConfig(){
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-application");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092, localhost:9093, localhost:9094");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        return config;
    }
    private static Topology createTopology(){
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> textLine = builder.stream("color-counter");

        KStream<String,String> userColour = textLine
                .filter((key, value)->value.contains(","))
                .selectKey((key, value)->value.split(",")[0].toLowerCase())
                .mapValues(value -> value.split(",")[1].toLowerCase())
                .filter((key, value)-> Arrays.asList("red", "green", "blue").contains(value));
        userColour.to("user-keys-and-colours-java");

        KTable<String, String> userColourTable = builder.table("user-keys-and-colours-java");

        KTable<String, Long> favoriteColour = userColourTable
                .groupBy((user,colour)->new KeyValue<>(colour,colour))
                .count(Named.as("countByCoulour"));

        favoriteColour.toStream().to("favourite-colour-output-java");

        return builder.build();
    }

    public static void main(String[] args) {
        KafkaStreams streams = new KafkaStreams(createTopology(),getConfig());
        streams.cleanUp();
        streams.start();

        System.out.println(streams);
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
