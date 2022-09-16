package pt.up.fe.ni.website.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.model.Post
import pt.up.fe.ni.website.backend.repository.PostRepository
import java.util.Date

class PostDTO {
    var title: String? = null
    var body: String? = null
    var href: String? = null
    var templatePath: String? = null
    var publishDate: Date? = null
}

@Service
class PostService(private val repository: PostRepository) {
    fun getAllPosts(): List<Post> = repository.findAll().toList()

    fun getPost(postID: Long): Post =
        repository.findByIdOrNull(postID.toLong()) ?: throw NoSuchElementException("Post Not Found")

    fun createPost(post: Post) = repository.save(post)

    fun updatePost(postID: Long, post: PostDTO) {
        val targetPost = repository.findByIdOrNull(postID) ?: throw NoSuchElementException("Post Not Found")
        targetPost.title = post.title ?: targetPost.title
        targetPost.body = post.body ?: targetPost.body
        targetPost.href = post.href ?: targetPost.href
        targetPost.templatePath = post.templatePath ?: targetPost.templatePath

        repository.save(targetPost)
    }

    fun deletePost(postID: Long) {
        repository.findByIdOrNull(postID) ?: throw NoSuchElementException("Post Not Found")
        repository.deleteById(postID)
    }
}
