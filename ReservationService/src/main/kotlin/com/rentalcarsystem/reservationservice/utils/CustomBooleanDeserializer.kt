package com.rentalcarsystem.reservationservice.utils

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.node.BooleanNode


class CustomBooleanDeserializer : JsonDeserializer<Boolean>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): Boolean {
        val node = p.codec.readTree<BooleanNode>(p)
        val value = node.asText()
        return when {
            value.equals("true", ignoreCase = true) -> true
            value.equals("false", ignoreCase = true) -> false
            else -> throw IllegalArgumentException("Invalid boolean value: $value")
        }
    }
}