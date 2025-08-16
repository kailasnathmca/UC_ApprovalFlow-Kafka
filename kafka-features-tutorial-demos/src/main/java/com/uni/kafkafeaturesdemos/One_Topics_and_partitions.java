package com.uni.kafkafeaturesdemos;

import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;


import org.apache.kafka.clients.admin.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class One_Topics_and_partitions {


    private AdminClient admin;

    public One_Topics_and_partitions() {
        admin = null;
    }

    public void KafkaTopicAdmin() {
        this.admin = AdminClient.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"));
    }

    public void ensureTopics() throws Exception {
        var topics = Set.of("proposal-events", "proposal-events.DLT", "audit-logs");
        var current = admin.listTopics().names().get(10, TimeUnit.SECONDS);
        var toCreate = new ArrayList<NewTopic>();
        if (!current.contains("proposal-events"))
            toCreate.add(new NewTopic("proposal-events", 3, (short) 1));
        if (!current.contains("proposal-events.DLT"))
            toCreate.add(new NewTopic("proposal-events.DLT", 3, (short) 1));
        if (!current.contains("audit-logs"))
            toCreate.add(new NewTopic("audit-logs", 1, (short) 1));
        if (!toCreate.isEmpty()) admin.createTopics(toCreate).all().get(10, TimeUnit.SECONDS);
    }
}
