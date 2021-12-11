package io.tral909

import java.util.concurrent.ConcurrentHashMap

class BlogService {
    private val blogPosts: Map<Int, BlogPost> = ConcurrentHashMap()


}