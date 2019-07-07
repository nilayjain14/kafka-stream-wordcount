package com.jainnil;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;

import java.util.Arrays;
import java.util.Properties;

public class KafkaStreamWordCount {

    public static void main(String[] args) {

        Properties config =  new Properties();
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,Serdes.String().getClass());
        config.put(StreamsConfig.APPLICATION_ID_CONFIG,"word-count");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KStreamBuilder builder = new KStreamBuilder();

        KStream<String, String> textLines = builder.stream("stream-word-count-input");
        KTable<String, Long> wordCounts = textLines
                .mapValues(textLine -> textLine.toLowerCase())
                .flatMapValues(textLine -> Arrays.asList(textLine.split(" ")))
                .selectKey((key, word) -> word)
                .groupByKey()
                .count("Counts");

        wordCounts.to(Serdes.String(), Serdes.Long(), "stream-word-count-output");

        KafkaStreams streams = new KafkaStreams(builder, config);
        streams.start();

        System.out.println(streams.toString());
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
