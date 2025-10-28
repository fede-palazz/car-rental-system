package com.rentalcarsystem.analyticsservice.config

import com.rentalcarsystem.analyticsservice.kafka.CarModelEventDTO
import com.rentalcarsystem.analyticsservice.kafka.MaintenanceEventDTO
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
class KafkaConsumerConfig(private val kafkaProperties: KafkaProperties) {

    @Bean
    fun carModelConsumerFactory(): ConsumerFactory<String, CarModelEventDTO> {
        val props = kafkaProperties.buildConsumerProperties()
        val deserializer = JsonDeserializer(CarModelEventDTO::class.java)
        return DefaultKafkaConsumerFactory(props, StringDeserializer(), deserializer)
    }

    @Bean
    fun carModelKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, CarModelEventDTO> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, CarModelEventDTO>()
        factory.consumerFactory = carModelConsumerFactory()
        return factory
    }

    @Bean
    fun maintenanceConsumerFactory(): ConsumerFactory<String, MaintenanceEventDTO> {
        val props = kafkaProperties.buildConsumerProperties()
        val deserializer = JsonDeserializer(MaintenanceEventDTO::class.java)
        return DefaultKafkaConsumerFactory(props, StringDeserializer(), deserializer)
    }

    @Bean
    fun maintenanceKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, MaintenanceEventDTO> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, MaintenanceEventDTO>()
        factory.consumerFactory = maintenanceConsumerFactory()
        return factory
    }
}