package pt.up.fe.ni.website.backend.service

object ErrorMessages {
    fun postNotFound(postId: Long): String {
        return "post not found with id $postId"
    }

    fun projectNotFound(id: Long): String {
        return "project not found with id $id"
    }

    fun emailAlreadyExists(): String {
        return "email already exists"
    }

    fun accountNotFound(id: Long): String {
        return "account not found with id $id"
    }

    fun emailNotFound(email: String): String {
        return "account not found with email $email";
    }

    fun invalidCredentials(): String {
        return "invalid credentials"
    }

    fun invalidRefreshToken(): String {
        return "invalid refresh token"
    }

    fun expiredRefreshToken(): String {
        return "refresh token has expired"
    }
}
