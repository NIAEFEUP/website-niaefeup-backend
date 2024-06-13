package pt.up.fe.ni.website.backend.model.seeders

import org.springframework.stereotype.Component

@Component
class ApplicationSeeder(
    private val accountSeeder: AccountSeeder
) {

    fun seedDatabase() {
        // TODO(luisd): delete database
        // TODO(luisd): apply schema

        // NOTE(luisd): you must consider the dependencies between seeders
        accountSeeder.createObjects()
    }
}
