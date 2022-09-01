package pt.up.fe.ni.website.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.model.Post
import pt.up.fe.ni.website.backend.repository.PostRepository

@Service
class PostService(private val repository: PostRepository) {
    fun getAllPosts(): Collection<Post> = repository.findAll().toList()

    fun getPost(postID : String): Post = repository.findByIdOrNull(postID.toLong()) ?: throw NoSuchElementException("Post Not Found")

    fun createPost(post: Post) = repository.save(post)

    fun updatePost(postID: String, newDescription: String?, newLink: String?) {
        val targetPost = repository.findByIdOrNull(postID.toLong()) ?: throw NoSuchElementException("Post Not Found")
        if (newDescription != null) {
            targetPost.description = newDescription
        };
        if (newLink != null) {
            targetPost.link = newLink
        }

        repository.save(targetPost)

    }
}