package pt.up.fe.ni.website.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import pt.up.fe.ni.website.backend.dto.entity.PostDto
import pt.up.fe.ni.website.backend.model.Post
import pt.up.fe.ni.website.backend.repository.PostRepository

@Service
class PostService(private val repository: PostRepository) {
    fun getAllPosts(): List<Post> = repository.findAll().toList()

    fun getPostById(postId: Long): Post =
        repository.findByIdOrNull(postId) ?: throw NoSuchElementException(ErrorMessages.postNotFound(postId))

    fun createPost(dto: PostDto): Post {
        repository.findBySlug(dto.slug)?.let {
            throw IllegalArgumentException(ErrorMessages.slugAlreadyExists)
        }

        val post = dto.create()
        return repository.save(post)
    }

    fun updatePostById(postId: Long, dto: PostDto): Post {
        val post = getPostById(postId)

        repository.findBySlug(dto.slug)?.let {
            throw IllegalArgumentException(ErrorMessages.slugAlreadyExists)
        }

        val newPost = dto.update(post)
        return repository.save(newPost)
    }

    fun deletePostById(postId: Long): Map<String, String> {
        repository.findByIdOrNull(postId) ?: throw NoSuchElementException(ErrorMessages.postNotFound(postId))
        repository.deleteById(postId)
        return mapOf()
    }

    fun getPostBySlug(postSlug: String): Post =
        repository.findBySlug(postSlug) ?: throw NoSuchElementException(ErrorMessages.postNotFound(postSlug))
}
