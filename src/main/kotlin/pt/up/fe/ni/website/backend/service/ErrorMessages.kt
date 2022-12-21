package pt.up.fe.ni.website.backend.service

object ErrorMessages {
    const val emailAlreadyExists = "email already exists"

    const val invalidCredentials = "invalid credentials"

    const val invalidRefreshToken = "invalid refresh token"

    const val expiredRefreshToken = "refresh token has expired"

    fun postNotFound(postId: Long): String = "post not found with id $postId"

    fun projectNotFound(id: Long): String = "project not found with id $id"

    fun accountNotFound(id: Long): String = "account not found with id $id"

    fun emailNotFound(email: String): String = "account not found with email $email"
}
