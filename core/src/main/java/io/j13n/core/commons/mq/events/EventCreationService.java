package io.j13n.core.commons.mq.events;

import io.j13n.core.commons.base.thread.VirtualThreadWrapper;
import io.j13n.core.commons.base.util.data.CircularLinkedList;
import io.j13n.core.commons.base.util.data.DoublePointerNode;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EventCreationService {

    @Value("${events.mq.exchange:events}")
    private String exchange;

    @Value("${events.mq.routingkeys:events1,events2,events3}")
    private String routingKey;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private DoublePointerNode<String> nextRoutingKey;

    @PostConstruct
    protected void init() {
        nextRoutingKey = new CircularLinkedList<>(this.routingKey.split(",")).getHead();
    }

    public CompletableFuture<Boolean> createEvent(EventQueObject queObj) {
        return VirtualThreadWrapper.fromCallable(() -> {
            this.nextRoutingKey = nextRoutingKey.getNext();
            amqpTemplate.convertAndSend(exchange, nextRoutingKey.getItem(), queObj);
            return true;
        });
    }
}
