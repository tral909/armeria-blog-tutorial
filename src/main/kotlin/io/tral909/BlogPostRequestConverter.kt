package io.tral909

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.linecorp.armeria.common.AggregatedHttpRequest
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.annotation.RequestConverterFunction
import java.lang.reflect.ParameterizedType
import java.util.concurrent.atomic.AtomicInteger


class BlogPostRequestConverter : RequestConverterFunction {

    companion object {
        private val mapper: ObjectMapper = ObjectMapper()
        private var idGenerator: AtomicInteger = AtomicInteger()

        fun stringValue(jsonNode: JsonNode, field: String): String {
            val value: JsonNode = jsonNode.get(field)
                ?: throw IllegalArgumentException("$field is missing!")
            return value.textValue()
        }
    }

    override fun convertRequest(
        ctx: ServiceRequestContext,
        request: AggregatedHttpRequest,
        expectedResultType: Class<out Any>,
        expectedParameterizedResultType: ParameterizedType?
    ): Any? {
        // It's strange, but if write '== BlogPost::class' branch execute, but atomicInteger doesn't work
        if (expectedResultType == BlogPost::class.java) {
            val jsonNode: JsonNode = mapper.readTree(request.contentUtf8())
            val id: Int = idGenerator.incrementAndGet()
            val title: String = stringValue(jsonNode, "title")
            val content: String = stringValue(jsonNode, "content")
            return BlogPost(id, title, content) // Create an instance of BlogPost object
        }
        return RequestConverterFunction.fallthrough()
    }
}