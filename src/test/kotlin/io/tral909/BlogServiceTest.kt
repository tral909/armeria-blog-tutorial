package io.tral909

import com.fasterxml.jackson.databind.ObjectMapper
import com.linecorp.armeria.client.WebClient
import com.linecorp.armeria.common.AggregatedHttpResponse
import com.linecorp.armeria.common.HttpRequest
import com.linecorp.armeria.common.HttpStatus
import com.linecorp.armeria.common.MediaType
import com.linecorp.armeria.server.ServerBuilder
import com.linecorp.armeria.testing.junit5.server.ServerExtension
import net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension


class BlogServiceTest {

    private val mapper = ObjectMapper()

    companion object {
        @RegisterExtension
        private val server = object : ServerExtension() {
            override fun configure(sb: ServerBuilder) {
                sb.annotatedService(BlogService())
            }

            override fun runForEachTest(): Boolean = true
        }
    }

    @Test
    fun createBlogPost() {
        val client = WebClient.of(server.httpUri())
        val request = createBlogPostRequest(mapOf(
                    "title" to "My first blog",
                    "content" to "Hello Armeria!"))

        val response: AggregatedHttpResponse = client.execute(request).aggregate().join()

        val expected = mapOf(
            "title" to "My first blog",
            "content" to "Hello Armeria!"
        )

        assertThatJson(response.contentUtf8()).whenIgnoringPaths("id", "createdAt", "modifiedAt")
            .isEqualTo(mapper.writeValueAsString(expected))
    }


    @Test
    fun getBlogPost() {
        val client = WebClient.of(server.httpUri())
        val request = createBlogPostRequest(mapOf(
            "title" to "My first blog",
            "content" to "Hello Armeria!"))
        var response = client.execute(request).aggregate().join()
        val id = mapper.readValue(response.contentUtf8(), BlogPost::class.java).id

        response = client["/blogs/$id"].aggregate().join()
        val expected = mapOf(
            "title" to "My first blog",
            "content" to "Hello Armeria!"
        )

        assertThatJson(response.contentUtf8()).whenIgnoringPaths("id", "createdAt", "modifiedAt")
            .isEqualTo(mapper.writeValueAsString(expected))
    }

    @Test
    fun getBlogPosts() {
        val client = WebClient.of(server.httpUri())
        var request = createBlogPostRequest(mapOf(
            "title" to "My first blog",
            "content" to "Hello Armeria!"))
        client.execute(request).aggregate().join()

        request = createBlogPostRequest(mapOf(
                    "title" to "My second blog",
                    "content" to "Armeria is awesome!"))
        client.execute(request).aggregate().join()

        val response = client["/blogs"].aggregate().join()
        val expected = listOf(
            mapOf(
                "title" to "My second blog",
                "content" to "Armeria is awesome!"),
            mapOf(
                "title" to "My first blog",
                "content" to "Hello Armeria!"
            )
        )

        assertThatJson(response.contentUtf8()).whenIgnoringPaths("[*].id", "[*].createdAt", "[*].modifiedAt")
            .isEqualTo(mapper.writeValueAsString(expected))
    }

    @Test
    fun updateBlogPost() {
        val client = WebClient.of(server.httpUri())
        val request = createBlogPostRequest(mapOf(
            "title" to "My first blog",
            "content" to "Hello Armeria!"))
        var response = client.execute(request).aggregate().join()
        val id = mapper.readValue(response.contentUtf8(), BlogPost::class.java).id

        val updatedContent = mapOf(
            "title" to "My first blog",
            "content" to "Hello awesome Armeria!"
        )
        val updateBlogPostRequest = HttpRequest.builder()
            .put("/blogs/$id")
            .content(MediaType.JSON_UTF_8, mapper.writeValueAsString(updatedContent))
            .build()
        client.execute(updateBlogPostRequest).aggregate().join()

        response = client.execute(HttpRequest.builder()
            .get("/blogs/$id").build()).aggregate().join()

        val expected = mapOf(
            "title" to "My first blog",
            "content" to "Hello awesome Armeria!"
        )
        assertThatJson(response.contentUtf8()).whenIgnoringPaths("id", "createdAt", "modifiedAt")
            .isEqualTo(mapper.writeValueAsString(expected))
    }

    @Test
    fun deleteBlogPost() {
        val client = WebClient.of(server.httpUri())
        val request = createBlogPostRequest(mapOf(
            "title" to "My first blog",
            "content" to "Hello Armeria!"))
        var response = client.execute(request).aggregate().join()
        val id = mapper.readValue(response.contentUtf8(), BlogPost::class.java).id

        response = client.delete("/blogs/$id").aggregate().join()
        assertSame(HttpStatus.NO_CONTENT, response.status())

        response = client["/blogs/$id"].aggregate().join()
        assertSame(HttpStatus.NOT_FOUND, response.status())
    }

    @Test
    fun badRequestExceptionHandlerWhenTryingDeleteMissingBlogPost() {
        val client = WebClient.of(server.httpUri())
        val response = client.delete("/blogs/100").aggregate().join()
        assertSame(HttpStatus.BAD_REQUEST, response.status())
        assertThatJson(response.contentUtf8()).isEqualTo("{\"error\":\"The blog post does not exist. id: 100\"}")
    }

    private fun createBlogPostRequest(content: Map<String, String>): HttpRequest {
        return HttpRequest.builder()
            .post("/blogs")
            .content(MediaType.JSON_UTF_8, mapper.writeValueAsString(content))
            .build()
    }

}