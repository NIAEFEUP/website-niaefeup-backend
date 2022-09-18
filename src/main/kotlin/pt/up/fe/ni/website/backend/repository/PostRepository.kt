package pt.up.fe.ni.website.backend.repository

import org.springframework.data.jpa.repository.JpaRepository
import pt.up.fe.ni.website.backend.model.Post

interface PostRepository : JpaRepository<Post, Long>
