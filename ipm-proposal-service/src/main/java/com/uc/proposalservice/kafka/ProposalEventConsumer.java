package com.uc.proposalservice.kafka;

import com.uc.common.ProposalEvent;
import com.uc.proposalservice.config.KafkaConfig;
import org.apache.commons.logging.Log; import org.apache.commons.logging.LogFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** Optional: see our own events flowing; safe to remove if not needed. */
@Component
public class ProposalEventConsumer {
    private static final Log log = LogFactory.getLog(ProposalEventConsumer.class);

    @KafkaListener(topics = KafkaConfig.EVENTS_TOPIC, groupId = "proposal-service")
    public void onEvent(@Payload ProposalEvent event) {
        log.info("proposal-service saw event: " + event.getType() + " pid=" + event.getProposalId());
    }
}
