package pt.up.fe.ni.website.backend.service

object ErrorMessages {
    const val slugAlreadyExists = "slug already exists"

    const val emailAlreadyExists = "email already exists"

    const val invalidCredentials = "invalid credentials"

    const val invalidRefreshToken = "invalid refresh token"

    const val expiredRefreshToken = "refresh token has expired"

    const val noGenerations = "no generations created yet"

    fun postNotFound(postId: Long): String = "post not found with id $postId"

    fun postNotFound(postSlug: String): String = "post not found with slug $postSlug"

    fun eventNotFound(eventSlug: String): String = "event not found with slug $eventSlug"

    fun projectNotFound(id: Long): String = "project not found with id $id"

    fun projectNotFound(projectSlug: String): String = "project not found with slug $projectSlug"

    fun eventNotFound(id: Long): String = "event not found with id $id"

    fun activityNotFound(id: Long): String = "activity not found with id $id"

    fun accountNotFound(id: Long): String = "account not found with id $id"

    fun generationNotFound(id: Long): String = "generation not found with id $id"

    fun generationNotFound(year: String): String = "generation not found with year $year"

    fun emailNotFound(email: String): String = "account not found with email $email"
}
