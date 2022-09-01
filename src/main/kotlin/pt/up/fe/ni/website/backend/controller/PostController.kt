package pt.up.fe.ni.website.backend.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.model.Post
import pt.up.fe.ni.website.backend.service.PostService

@RestController
@RequestMapping("/posts")
class PostController(private val service: PostService) {
    @GetMapping
    fun getAllPosts(): Collection<Post> = service.getAllPosts()

    @PostMapping("/new")
    fun createPost(@RequestBody post: Post) = service.createPost(post)
}