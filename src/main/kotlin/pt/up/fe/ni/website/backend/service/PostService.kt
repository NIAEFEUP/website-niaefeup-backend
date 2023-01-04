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
        val post = dto.create()
        return repository.save(post)
    }

    fun updatePostById(postId: Long, dto: PostDto): Post {
        val project = getPostById(postId)
        val newProject = dto.update(project)
        return repository.save(newProject)
    }

    fun deletePostById(postId: Long): Map<String, String> {
        repository.findByIdOrNull(postId) ?: throw NoSuchElementException(ErrorMessages.postNotFound(postId))
        repository.deleteById(postId)
        return mapOf()
    }
}
