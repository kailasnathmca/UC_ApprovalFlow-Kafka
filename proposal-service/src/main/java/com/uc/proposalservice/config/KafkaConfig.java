package com.uc.proposalservice.config;

import com.uc.common.ProposalEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Configures Kafka producers/consumers and auto-creates topics for local dev.
 * - proposal-events: JSON events
 * - proposal-events.DLT: dead letter topic
 * - audit-logs: plain string audit lines
 */
@EnableKafka
@Configuration
public class KafkaConfig {
    public static final String EVENTS_TOPIC = "proposal-events";
    public static final String AUDIT_TOPIC = "audit-logs";
    public static final String DLT_SUFFIX = ".DLT";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ---- Producers ----
    @Bean
    public ProducerFactory<String, ProposalEvent> eventProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }
    @Bean public KafkaTemplate<String, ProposalEvent> eventKafkaTemplate() {
        return new KafkaTemplate<>(eventProducerFactory());
    }

    @Bean
    public ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }
    @Bean public KafkaTemplate<String, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }

    // ---- Consumer factory with simple retry then DLT ----
    @Bean
    public ConsumerFactory<String, ProposalEvent> eventConsumerFactory() {
        JsonDeserializer<ProposalEvent> json = new JsonDeserializer<>(ProposalEvent.class);
        json.addTrustedPackages("*");
        Map<String, Object> props = new HashMap<>();
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, "proposal-service");
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), json);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProposalEvent> kafkaListenerContainerFactory(
            KafkaTemplate<String, ProposalEvent> dltTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, ProposalEvent> f = new ConcurrentKafkaListenerContainerFactory<>();
        f.setConsumerFactory(eventConsumerFactory());
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                dltTemplate,
                (record, ex) -> new org.apache.kafka.common.TopicPartition(record.topic() + DLT_SUFFIX, record.partition())
        );
        f.setCommonErrorHandler(new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L)));
        f.setConcurrency(2);
        return f;
    }

    // ---- Auto-create topics (works when broker allows) ----
    @Bean public NewTopic proposalEventsTopic() { return new NewTopic(EVENTS_TOPIC, 3, (short) 1); }
    @Bean public NewTopic proposalEventsDltTopic() { return new NewTopic(EVENTS_TOPIC + DLT_SUFFIX, 3, (short) 1); }
    @Bean public NewTopic auditTopic() { return new NewTopic(AUDIT_TOPIC, 1, (short) 1); }
}
