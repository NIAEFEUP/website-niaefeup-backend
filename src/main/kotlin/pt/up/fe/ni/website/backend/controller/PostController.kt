package pt.up.fe.ni.website.backend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.up.fe.ni.website.backend.model.Event
import pt.up.fe.ni.website.backend.model.Post
import pt.up.fe.ni.website.backend.model.dto.PostDto
import pt.up.fe.ni.website.backend.service.PostService

@RestController
@Tag(name = "Posts", description = "Post related endpoints")
@RequestMapping("/posts")
class PostController(private val service: PostService) {

    @Operation(summary = "Gets all posts", description = "Returns the list off all posts in the content")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Successful Operation", content = arrayOf(Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = Post::class)))))])
    @GetMapping
    fun getAllPosts() = service.getAllPosts()

    @Operation(summary = "Gets an event by id", description = "Returns a single post")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Successful Operation", content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = Event::class))))])
    @GetMapping("/{postId}")
    fun getPost(@PathVariable postId: Long) = service.getPostById(postId)

    @Operation(summary = "Create a new post", description = "Returns the newly created post")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Successful Operation", content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = Event::class))))])
    @PostMapping("/new")
    fun createPost(@RequestBody dto: PostDto) = service.createPost(dto)

    @Operation(summary = "Updates a post", description = "Returns the updated post")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Successful Operation", content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = Event::class))))])
    @PutMapping("/{postId}")
    fun updatePost(@PathVariable postId: Long, @RequestBody dto: PostDto) = service.updatePostById(postId, dto)

    @Operation(summary = "Deletes a post", description = "Returns an empty map")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Successful Operation", content = arrayOf(Content(mediaType = "application/json", schema = Schema(implementation = Map::class))))])
    @DeleteMapping("/{postId}")
    fun deletePost(@PathVariable postId: Long) = service.deletePostById(postId)
}
