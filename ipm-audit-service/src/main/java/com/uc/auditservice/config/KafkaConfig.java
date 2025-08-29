package com.uc.auditservice.config;

import com.uc.common.ProposalEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;

/** Minimal consumer config for audit-service. */
@EnableKafka
@Configuration
public class KafkaConfig {
    public static final String EVENTS_TOPIC = "proposal-events";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, ProposalEvent> eventConsumerFactory() {
        JsonDeserializer<ProposalEvent> json = new JsonDeserializer<>(ProposalEvent.class);
        json.addTrustedPackages("*");
        var props = new java.util.HashMap<String,Object>();
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, "audit-service");
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), json);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProposalEvent> kafkaListenerContainerFactory() {
        var f = new ConcurrentKafkaListenerContainerFactory<String, ProposalEvent>();
        f.setConsumerFactory(eventConsumerFactory());
        f.setConcurrency(2);
        return f;
    }
}
