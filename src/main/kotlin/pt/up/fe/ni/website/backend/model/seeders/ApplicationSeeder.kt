package pt.up.fe.ni.website.backend.model.seeders

class ApplicationSeeder {

    //NOTE(luisd): these are run in order so you should take care of dependencies of said seeders
    private val seeders : List<AbstractSeeder> = listOf(
        AccountSeeder()
    )


    fun seedDatabase(){
        //TODO(luisd): delete database
        //TODO(luisd): apply schema

        seeders.forEach {
            seeder -> seeder.createObjects()
        }
    }
}
