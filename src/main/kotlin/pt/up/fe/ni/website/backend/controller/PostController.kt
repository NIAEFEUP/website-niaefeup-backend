package pt.up.fe.ni.website.backend.controller

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.model.Post
import pt.up.fe.ni.website.backend.service.PostService
import javax.validation.Valid

@RestController
@RequestMapping("/posts")
class PostController(private val service: PostService) {

    @GetMapping
    fun getAllPosts(): Collection<Post> = service.getAllPosts()

    @GetMapping("/{PostId}")
    fun getPost(@PathVariable PostId: Long): Post = service.getPost(PostId)

    @PostMapping("/new")
    fun createPost(
        @Valid @RequestBody
        post: Post
    ) = service.createPost(post)

    @PatchMapping("/{PostId}")
    fun updatePost(
        @PathVariable PostId: Long,
        @Valid @RequestBody
        post: Post.PatchModel
    ) = service.updatePost(PostId, post)

    @DeleteMapping("/{PostId}")
    fun deletePost(@PathVariable PostId: Long) = service.deletePost(PostId)
}
