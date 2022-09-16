package pt.up.fe.ni.website.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.model.Post
import pt.up.fe.ni.website.backend.repository.PostRepository
import java.util.Date

class PostDTO {
    val description: String? = null
    val link: String? = null
    val date: Date? = null
}

@Service
class PostService(private val repository: PostRepository) {
    fun getAllPosts(): List<Post> = repository.findAll().toList()

    fun getPost(postID: Long): Post =
        repository.findByIdOrNull(postID.toLong()) ?: throw NoSuchElementException("Post Not Found")

    fun createPost(post: Post) = repository.save(post)

    fun updatePost(postID: Long, post: PostDTO) {
        val targetPost = repository.findByIdOrNull(postID) ?: throw NoSuchElementException("Post Not Found")
        targetPost.description = post.description ?: targetPost.description
        targetPost.link = post.link ?: targetPost.link
        targetPost.date = post.date ?: targetPost.date

        repository.save(targetPost)
    }

    fun deletePost(postID: Long) {
        repository.findByIdOrNull(postID) ?: throw NoSuchElementException("Post Not Found")
        repository.deleteById(postID)
    }
}
