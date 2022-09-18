package pt.up.fe.ni.website.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.model.Post
import pt.up.fe.ni.website.backend.repository.PostRepository

class PostDto {
    var title: String? = null
    var body: String? = null
    var thumbnailPath: String? = null
}

@Service
class PostService(private val repository: PostRepository) {
    fun getAllPosts(): List<Post> = repository.findAll().toList()

    fun getPost(PostId: Long): Post =
        repository.findByIdOrNull(PostId) ?: throw NoSuchElementException("Post Not Found")

    fun createPost(post: Post) = repository.save(post)

    fun updatePost(PostId: Long, post: PostDto): Post {
        val targetPost = repository.findByIdOrNull(PostId) ?: throw NoSuchElementException("Post Not Found")
        targetPost.title = post.title ?: targetPost.title
        targetPost.body = post.body ?: targetPost.body
        targetPost.thumbnailPath = post.thumbnailPath ?: targetPost.thumbnailPath

        return repository.save(targetPost)
    }

    fun deletePost(PostId: Long): Map<String, String> {
        repository.findByIdOrNull(PostId) ?: throw NoSuchElementException("Post Not Found")
        repository.deleteById(PostId)

        return mapOf()
    }
}
