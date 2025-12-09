package com.viana.poc.service;

import com.viana.avro.AccountEvent;
import com.viana.avro.EventType;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import static com.viana.poc.constants.Constants.ACCOUNT_BALANCE_STORE;
import static com.viana.poc.constants.Constants.ACCOUNT_EVENTS_TOPIC;

@Configuration
@EnableKafkaStreams
public class AccountBalanceTopology {

    @Bean
    public KTable<String, Double> accountBalances(StreamsBuilder builder,
                                                  SpecificAvroSerde<AccountEvent> accountEventSerde) {

        KStream<String, AccountEvent> events = builder.stream(
                ACCOUNT_EVENTS_TOPIC,
                Consumed.with(Serdes.String(), accountEventSerde)
        );

        KStream<String, AccountEvent> eventsByAccount =
                events.selectKey((ignoredKey, event) -> event.getAccountId());

        KGroupedStream<String, AccountEvent> grouped =
                eventsByAccount.groupByKey(Grouped.with(Serdes.String(), accountEventSerde));

        // Make types explicit so the compiler stops complaining

        Initializer<Double> initializer = () -> 0.0d;

        Aggregator<String, AccountEvent, Double> aggregator =
                (String accountId, AccountEvent event, Double current) -> {
                    if (current == null) {
                        current = 0.0d;
                    }

                    double amount = event.getAmount();
                    EventType type = event.getEventType();

                    return switch (type) {
                        case CREDIT -> current + amount;
                        case DEBIT  -> current - amount;
                    };
                };

        KTable<String, Double> balances = grouped.aggregate(
                initializer,
                aggregator,
                Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>as(ACCOUNT_BALANCE_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Double())
        );

        balances.toStream().foreach((accountId, balance) ->
                System.out.printf("KTable → account %s new balance = %.2f%n", accountId, balance)
        );

        return balances;
    }
}
