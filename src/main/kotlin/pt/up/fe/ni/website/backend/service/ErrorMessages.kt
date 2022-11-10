package pt.up.fe.ni.website.backend.service

object ErrorMessages {
    fun postNotFound(postId: Long): String {
        return "post not found with id $postId"
    }

    fun projectNotFound(id: Long): String {
        return "project not found with id $id"
    }
}
