package com.uc.notificationservice.kafka;

import com.uc.common.ProposalEvent;
import com.uc.notificationservice.config.KafkaConfig;
import org.apache.commons.logging.Log; import org.apache.commons.logging.LogFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Simulates sending notifications. In real life, inject an Email/SMS gateway
 * and branch behavior by event type or payload content.
 */
@Component
public class NotificationListener {
    private static final Log log = LogFactory.getLog(NotificationListener.class);

    @KafkaListener(topics = KafkaConfig.EVENTS_TOPIC, groupId = "notification-service")
    public void onEvent(@Payload ProposalEvent ev){
        log.info("notification-service would notify: " + ev.getType() + " for proposal " + ev.getProposalId());
    }
}
