package com.lazybird.worker.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import com.lazybird.common.messaging.OrderMessagingConstants;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange ordersExchange() {
        return new DirectExchange(OrderMessagingConstants.ORDERS_EXCHANGE, true, false);
    }

    @Bean
    public Queue processQueue() {
        return QueueBuilder.durable(OrderMessagingConstants.PROCESS_QUEUE)
                .withArgument("x-dead-letter-exchange", OrderMessagingConstants.ORDERS_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", OrderMessagingConstants.DLQ_KEY)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(OrderMessagingConstants.DLQ_QUEUE)
                .build();
    }

    @Bean
    public Binding processBinding() {
        return BindingBuilder.bind(processQueue())
                .to(ordersExchange())
                .with(OrderMessagingConstants.PROCESS_KEY);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(ordersExchange())
                .with(OrderMessagingConstants.DLQ_KEY);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

}
