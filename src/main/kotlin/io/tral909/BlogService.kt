package io.tral909

import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.server.annotation.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.NoSuchElementException


class BlogService {
    private val blogPosts: MutableMap<Int, BlogPost> = ConcurrentHashMap()

    @Post("/blogs")
    @RequestConverter(BlogPostRequestConverter::class)
    fun createBlogPost(blogPost: BlogPost): HttpResponse {
        blogPosts[blogPost.id] = blogPost
        return HttpResponse.ofJson(blogPost)
    }

    @Get("/blogs/:id")
    fun getBlogPost(@Param("id") id: Int): HttpResponse =
        HttpResponse.ofJson(blogPosts[id] ?: throw NoSuchElementException("Post with id = $id is not found!"))

    @Get("/blogs")
    @ProducesJson
    fun getBlogPosts(@Param("desc") @Default("true") descending: Boolean): Iterable<BlogPost> {
        // Descending
        if (descending) {
            return blogPosts.entries
                .sortedByDescending { it.key }
                .map { it.value }
                .toCollection(mutableListOf())
        }
        // Ascending
        return blogPosts.values
    }
}