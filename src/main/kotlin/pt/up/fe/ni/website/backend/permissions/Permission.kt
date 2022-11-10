package pt.up.fe.ni.website.backend.permissions

enum class Permission(val bit: Long) {
    CREATE_ACCOUNT(0), VIEW_ACCOUNT(1), EDIT_ACCOUNT(2), DELETE_ACCOUNT(3),
    CREATE_ACTIVITY(4), VIEW_ACTIVITY(5), EDIT_ACTIVITY(6), DELETE_ACTIVITY(7),
    EDIT_SETTINGS(8), SUPERUSER(9)
}
