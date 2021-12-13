package io.tral909

import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.server.annotation.Post
import com.linecorp.armeria.server.annotation.RequestConverter
import java.util.concurrent.ConcurrentHashMap

class BlogService {
    private val blogPosts: MutableMap<Int, BlogPost> = ConcurrentHashMap()

    @Post("/blogs")
    @RequestConverter(BlogPostRequestConverter::class)
    fun createBlogPost(blogPost: BlogPost): HttpResponse {
        blogPosts[blogPost.id] = blogPost
        return HttpResponse.ofJson(blogPost)
    }
}