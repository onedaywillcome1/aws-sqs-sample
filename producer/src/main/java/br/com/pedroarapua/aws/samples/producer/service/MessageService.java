package br.com.pedroarapua.aws.samples.producer.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private static final int MAX_BATCH_SEND_SQS = 10;

    @Value("${consumer.sqs.message.queue.name}")
    private String queueName;

    @Autowired
    private AmazonSQS amazonSQS;
    
    Logger logger = LoggerFactory.getLogger(MessageService.class);

    public void sentToQueue(String message) {
    	logger.info("Mensagem a ser enviada => " + message);
    	final SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withQueueUrl(queueName)
                .withMessageBody(message);

        amazonSQS.sendMessage(sendMessageRequest);
    }

    public void sentToQueue(List<String> messages) {
        Lists.partition(messages, MAX_BATCH_SEND_SQS)
                .forEach(strings -> {
                    final AtomicInteger index = new AtomicInteger();
                    final List<SendMessageBatchRequestEntry> entries = strings.stream()
                            .map(message -> {
                                final String messageId = String.valueOf(index.getAndIncrement());
                                return new SendMessageBatchRequestEntry(messageId, message);
                            })
                            .collect(Collectors.toList());

                    final SendMessageBatchRequest sendMessageRequest = new SendMessageBatchRequest()
                            .withQueueUrl(queueName)
                            .withEntries(entries);

                    amazonSQS.sendMessageBatch(sendMessageRequest);
                });
    }

    public void sentToQueueWithAttributes(String message) {
        final Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("id",
                new MessageAttributeValue()
                 .withDataType("String")
                 .withStringValue(UUID.randomUUID().toString()));

        messageAttributes.put("date",
                new MessageAttributeValue()
                        .withDataType("String")
                        .withStringValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))));

        final SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withQueueUrl(queueName)
                .withMessageAttributes(messageAttributes)
                .withMessageBody(message);

        amazonSQS.sendMessage(sendMessageRequest);
    }

}
