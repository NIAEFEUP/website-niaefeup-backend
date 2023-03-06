package pt.up.fe.ni.website.backend.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pt.up.fe.ni.website.backend.model.Post

@Repository
interface PostRepository : JpaRepository<Post, Long> {
    fun findBySlug(slug: String?): Post?
}
