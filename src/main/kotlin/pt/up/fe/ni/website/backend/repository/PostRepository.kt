package pt.up.fe.ni.website.backend.repository

import org.springframework.data.repository.CrudRepository
import pt.up.fe.ni.website.backend.model.Post

interface PostRepository : CrudRepository<Post, Long>
