package pt.up.fe.ni.website.backend.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import pt.up.fe.ni.website.backend.model.TimelineEvent

@Repository
interface TimeLineEventRepository : CrudRepository<TimelineEvent, Long>
