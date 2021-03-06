package br.com.pedroarapua.aws.sample.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConsumerListener {
	Logger logger = LoggerFactory.getLogger(ConsumerListener.class);

    @JmsListener(destination = "${consumer.sqs.message.queue.name}")
    public void messageConsumer(@Headers Map<String, Object> messageAttributes,
                       @Payload String message) {
    	logger.info("Messages attributes: " + messageAttributes);
    	logger.info("Body: " + message);
    }

}
