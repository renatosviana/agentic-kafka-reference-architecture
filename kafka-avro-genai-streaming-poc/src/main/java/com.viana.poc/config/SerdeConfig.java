package com.viana.poc.config;

import com.viana.avro.AccountEvent;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class SerdeConfig {

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Bean
    public Serde<String> stringSerde() {
        return Serdes.String();
    }

    @Bean
    public Serde<Double> doubleSerde() {
        return Serdes.Double();
    }

    @Bean
    public SpecificAvroSerde<AccountEvent> accountEventSerde() {
        SpecificAvroSerde<AccountEvent> serde = new SpecificAvroSerde<>();

        Map<String, Object> props = Map.of(
                "schema.registry.url", schemaRegistryUrl,
                "specific.avro.reader", true
        );

        // false = value serde
        serde.configure(props, false);
        return serde;
    }
}
