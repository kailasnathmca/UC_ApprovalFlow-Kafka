package com.uc.auditservice.kafka;

import com.uc.auditservice.service.AuditService;
import com.uc.common.ProposalEvent;
import com.uc.auditservice.config.KafkaConfig;
import org.apache.commons.logging.Log; import org.apache.commons.logging.LogFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** Consumes 'proposal-events' and persists them. */
@Component
public class ProposalEventListener {
    private static final Log log = LogFactory.getLog(ProposalEventListener.class);
    private final AuditService audit;
    public ProposalEventListener(AuditService audit){ this.audit = audit; }

    @KafkaListener(topics = KafkaConfig.EVENTS_TOPIC, groupId = "audit-service")
    public void onEvent(@Payload ProposalEvent ev){
        log.info("audit-service storing event " + ev.getType() + " for proposal " + ev.getProposalId());
        audit.save(ev);
    }
}
