package pt.up.fe.ni.website.backend.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.up.fe.ni.website.backend.model.Post
import pt.up.fe.ni.website.backend.service.PostService
import kotlin.NoSuchElementException

@RestController
@RequestMapping("/posts")
class PostController(private val service: PostService) {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(e : NoSuchElementException): ResponseEntity<String> =
            ResponseEntity(e.message, HttpStatus.NOT_FOUND)

    @GetMapping
    fun getAllPosts(): Collection<Post> = service.getAllPosts()

    @GetMapping("/{postID}")
    fun getPost(@PathVariable postID: String): Post = service.getPost(postID)

    @PostMapping("/new")
    fun createPost(@RequestBody post: Post) = service.createPost(post)

    @PatchMapping("/{postID}")
    fun updatePost(@PathVariable postID: String, @RequestBody json: Map<String, String>) = service.updatePost(postID, json["description"], json["link"])
}