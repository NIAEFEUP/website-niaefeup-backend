package pt.up.fe.ni.website.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.model.Post
import pt.up.fe.ni.website.backend.repository.PostRepository

@Service
class PostService(private val repository: PostRepository) {
    fun getAllPosts(): List<Post> = repository.findAll().toList()

    fun getPost(postId: Long): Post =
        repository.findByIdOrNull(postId) ?: throw NoSuchElementException("post not found with id $postId")

    fun createPost(post: Post): Post = repository.save(post)

    fun updatePost(postId: Long, post: Post.PatchModel): Post {
        val targetPost =
            repository.findByIdOrNull(postId) ?: throw NoSuchElementException("post not found with id $postId")
        targetPost.title = post.title ?: targetPost.title
        targetPost.body = post.body ?: targetPost.body
        targetPost.thumbnailPath = post.thumbnailPath ?: targetPost.thumbnailPath

        return repository.save(targetPost)
    }

    fun deletePost(postId: Long): Map<String, String> {
        repository.findByIdOrNull(postId) ?: throw NoSuchElementException("post not found with id $postId")
        repository.deleteById(postId)

        return mapOf()
    }
}
