package pt.up.fe.ni.website.backend.repository

import org.springframework.stereotype.Repository
import pt.up.fe.ni.website.backend.model.Project

@Repository
interface ProjectRepository : ActivityRepository<Project>
