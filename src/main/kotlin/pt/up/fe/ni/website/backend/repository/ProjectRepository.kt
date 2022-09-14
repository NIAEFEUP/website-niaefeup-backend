package pt.up.fe.ni.website.backend.repository

import org.springframework.data.repository.CrudRepository
import pt.up.fe.ni.website.backend.model.Project

interface ProjectRepository : CrudRepository<Project, Long>
