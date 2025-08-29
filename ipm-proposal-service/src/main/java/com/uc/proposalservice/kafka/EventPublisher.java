package com.uc.proposalservice.kafka;

import com.uc.common.ProposalEvent;
import com.uc.proposalservice.config.KafkaConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Publishes domain events to 'proposal-events' and a simple string line to 'audit-logs'.
 * The audit line is handy to see something immediately in Kafka UI.
 */
@Component
public class EventPublisher {
    private final KafkaTemplate<String, ProposalEvent> eventTemplate;
    private final KafkaTemplate<String, String> stringTemplate;

    public EventPublisher(KafkaTemplate<String, ProposalEvent> eventTemplate,
                          KafkaTemplate<String, String> stringTemplate) {
        this.eventTemplate = eventTemplate;
        this.stringTemplate = stringTemplate;
    }

    public void publish(ProposalEvent ev) {
        if (ev.getId() == null) ev.setId(UUID.randomUUID().toString());
        eventTemplate.send(KafkaConfig.EVENTS_TOPIC, String.valueOf(ev.getProposalId()), ev);
        stringTemplate.send(KafkaConfig.AUDIT_TOPIC,
                "[" + ev.getAt() + "] " + ev.getType() + " proposalId=" + ev.getProposalId() + " payload=" + ev.getPayload());
    }
}
