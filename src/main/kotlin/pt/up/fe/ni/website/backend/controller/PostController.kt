package pt.up.fe.ni.website.backend.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.dto.entity.PostDto
import pt.up.fe.ni.website.backend.service.PostService

@RestController
@RequestMapping("/posts")
class PostController(private val service: PostService) {

    @GetMapping
    fun getAllPosts() = service.getAllPosts()

    @GetMapping("/{postId:\\d+}")
    fun getPost(@PathVariable postId: Long) = service.getPostById(postId)

    @GetMapping("/{postSlug}**")
    fun getPost(@PathVariable postSlug: String) = service.getPostBySlug(postSlug)

    @PostMapping
    fun createPost(@RequestBody dto: PostDto) = service.createPost(dto)

    @PutMapping("/{postId}")
    fun updatePost(
        @PathVariable postId: Long,
        @RequestBody dto: PostDto
    ) = service.updatePostById(postId, dto)

    @DeleteMapping("/{postId}")
    fun deletePost(@PathVariable postId: Long): Map<String, String> {
        service.deletePostById(postId)
        return emptyMap()
    }
}
