package io.tral909

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class BlogPost @JsonCreator constructor(
    @JsonProperty("id") val id: Int,
    @JsonProperty("title") val title: String,
    @JsonProperty("content") val content: String,
    @JsonProperty("createdAt") val createdAt: Long = System.currentTimeMillis(),
    @JsonProperty("modifiedAt") val modifiedAt: Long = System.currentTimeMillis()
)