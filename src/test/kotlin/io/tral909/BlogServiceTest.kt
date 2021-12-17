package io.tral909

import com.fasterxml.jackson.databind.ObjectMapper
import com.linecorp.armeria.client.WebClient
import com.linecorp.armeria.common.AggregatedHttpResponse
import com.linecorp.armeria.common.HttpRequest
import com.linecorp.armeria.common.MediaType
import com.linecorp.armeria.server.ServerBuilder
import com.linecorp.armeria.testing.junit5.server.ServerExtension
import net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension


class BlogServiceTest {

    private val mapper = ObjectMapper()
    private val client = WebClient.of(server.httpUri())

    companion object {
        @RegisterExtension
        private val server = object : ServerExtension() {
            override fun configure(sb: ServerBuilder) {
                sb.annotatedService(BlogService())
            }
        }
    }

    @Test
    @Order(1)
    fun createBlogPost() {
        val request = HttpRequest.builder()
            .post("/blogs")
            .content(MediaType.JSON_UTF_8, mapper.writeValueAsString(
                mapOf(
                    "title" to "My first blog",
                    "content" to "Hello Armeria!")))
            .build()

        val response: AggregatedHttpResponse = client.execute(request).aggregate().join()

        val expected = mapOf(
            "id" to 1,
            "title" to "My first blog",
            "content" to "Hello Armeria!"
        )

        assertThatJson(response.contentUtf8()).whenIgnoringPaths("createdAt", "modifiedAt")
            .isEqualTo(mapper.writeValueAsString(expected))

    }


    @Test
    @Order(2)
    fun getBlogPost() {
        val res = client["/blogs/1"].aggregate().join()
        val expected = mapOf(
            "id" to 1,
            "title" to "My first blog",
            "content" to "Hello Armeria!"
        )

        assertThatJson(res.contentUtf8()).whenIgnoringPaths("createdAt", "modifiedAt")
            .isEqualTo(mapper.writeValueAsString(expected))
    }
}