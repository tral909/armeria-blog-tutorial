package io.tral909

import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.common.HttpStatus
import com.linecorp.armeria.server.annotation.Blocking
import com.linecorp.armeria.server.annotation.Default
import com.linecorp.armeria.server.annotation.Delete
import com.linecorp.armeria.server.annotation.ExceptionHandler
import com.linecorp.armeria.server.annotation.Get
import com.linecorp.armeria.server.annotation.Param
import com.linecorp.armeria.server.annotation.Post
import com.linecorp.armeria.server.annotation.ProducesJson
import com.linecorp.armeria.server.annotation.Put
import com.linecorp.armeria.server.annotation.RequestConverter
import com.linecorp.armeria.server.annotation.RequestObject
import java.util.concurrent.ConcurrentHashMap


class BlogService {
    private val blogPosts: MutableMap<Int, BlogPost> = ConcurrentHashMap()

    @Post("/blogs")
    @RequestConverter(BlogPostRequestConverter::class)
    fun createBlogPost(blogPost: BlogPost): HttpResponse {
        blogPosts[blogPost.id] = blogPost
        return HttpResponse.ofJson(blogPost)
    }

    @Get("/blogs/:id")
    fun getBlogPost(@Param("id") id: Int): HttpResponse {
        val blogPost = blogPosts[id] ?: return HttpResponse.of(HttpStatus.NOT_FOUND)
        return HttpResponse.ofJson(blogPost)
    }

    @Get("/blogs")
    @ProducesJson // надо для возврата массива объектов
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

    @Put("/blogs/:id")
    fun updateBlogPost(@Param("id") id: Int, @RequestObject blogPost: BlogPost): HttpResponse {
        val oldBlogPost: BlogPost = blogPosts[id] ?: return HttpResponse.of(HttpStatus.NOT_FOUND)
        val newBlogPost = BlogPost(id, blogPost.title, blogPost.content, oldBlogPost.createdAt, blogPost.createdAt)
        blogPosts[id] = newBlogPost
        return HttpResponse.ofJson(newBlogPost)
    }

    @Blocking
    @Delete("/blogs/:id")
    @ExceptionHandler(BlogRequestExceptionHandler::class)
    fun deleteBlogPost(@Param("id") id: Int): HttpResponse {
        val removed = blogPosts.remove(id) ?: throw IllegalArgumentException("The blog post does not exist. id: $id")
        return HttpResponse.of(HttpStatus.NO_CONTENT)
    }
}