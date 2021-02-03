package com.dreamhunter.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;

@Slf4j
@Configuration
public class RedisConfig {

    @Bean
    StreamMessageListenerContainer streamContainer(
            RedisConnectionFactory redisConnectionFactory,
            StreamMessageListenerContainerOptions streamMessageListenerContainerOptions)
    {
        return StreamMessageListenerContainer.create(
                redisConnectionFactory, streamMessageListenerContainerOptions
        );
    }

    @Bean
    StreamMessageListenerContainerOptions streamMessageListenerContainerOptions(
            ThreadPoolTaskExecutor threadPoolTaskExecutor
    ){
        return StreamMessageListenerContainerOptions
                .builder()
                // 一次性最多拉取多少条消息
                .batchSize(10)
                // 消费消息的线程池
                .executor(threadPoolTaskExecutor)
                // 消息消费异常的handler
                .errorHandler(Throwable::printStackTrace)
                // 超时时间，设置为0，表示不超时（超时后会抛出异常）
                .pollTimeout(Duration.ZERO)
                // 序列化器
                .serializer(new StringRedisSerializer())
                .build();
    }
}