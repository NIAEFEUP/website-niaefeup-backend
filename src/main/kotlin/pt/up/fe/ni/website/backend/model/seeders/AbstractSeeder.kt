package pt.up.fe.ni.website.backend.model.seeders

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
abstract class AbstractSeeder<T, U, P> where T : CrudRepository<U, P> {

    @Autowired
    protected lateinit var repository: T

    abstract fun createObjects()
}
