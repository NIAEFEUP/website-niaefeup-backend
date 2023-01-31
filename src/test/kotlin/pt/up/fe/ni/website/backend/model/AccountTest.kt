package pt.up.fe.ni.website.backend.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import pt.up.fe.ni.website.backend.model.permissions.Permission
import pt.up.fe.ni.website.backend.model.permissions.Permissions

class AccountTest {

    @Test
    fun `effective permissions should be correct`() {
        val memberRole = Role(
            "Membro",
            Permissions(
                listOf(Permission.VIEW_ACCOUNT, Permission.VIEW_ACTIVITY),
            ),
            true,
        )

        val websiteManagerRole = Role(
            "Website Manager",
            Permissions(),
            false,
        )

        val websiteDeveloperRole = Role(
            "Website Developer",
            Permissions(),
            true,
        )

        val websiteProject = Project(
            "NI Website",
            "NI's website is where everything about NI is shown to the public",
        )

        val managerAccount = Account(
            "Smart manager #1",
            "website.manager@ni.fe.up.pt",
            "********",
            "This is a simple test account",
            null,
            null,
            null,
            null,
            emptyList(),
            mutableListOf(
                memberRole,
                websiteManagerRole,
                websiteDeveloperRole,
            ),
        )

        val developerAccount = Account(
            "Smart developer #2",
            "website.developer@ni.fe.up.pt",
            "********",
            "This is another simple test account",
            null,
            null,
            null,
            null,
            emptyList(),
            mutableListOf(
                memberRole,
                websiteDeveloperRole,
            ),
        )

        val memberAccount = Account(
            "Member #3",
            "member@ni.fe.up.pt",
            "********",
            "This is yet another simple test account",
            null,
            null,
            null,
            null,
            emptyList(),
            mutableListOf(
                memberRole,
            ),
        )

        memberRole.accounts.addAll(
            listOf(
                managerAccount,
                developerAccount,
                memberAccount,
            ),
        )

        websiteManagerRole.accounts.addAll(
            listOf(
                managerAccount,
            ),
        )
        websiteDeveloperRole.accounts.addAll(
            listOf(
                managerAccount,
                developerAccount,
            ),
        )

        val websiteManagerToProject = PerActivityRole(
            Permissions(
                listOf(Permission.EDIT_SETTINGS, Permission.DELETE_ACTIVITY),
            ),
        )
        websiteManagerToProject.role = websiteManagerRole
        websiteManagerToProject.activity = websiteProject

        val websiteDeveloperToProject = PerActivityRole(
            Permissions(
                listOf(Permission.VIEW_ACTIVITY, Permission.EDIT_ACTIVITY),
            ),
        )
        websiteDeveloperToProject.role = websiteDeveloperRole
        websiteDeveloperToProject.activity = websiteProject

        websiteProject.associatedRoles.addAll(
            listOf(
                websiteManagerToProject,
                websiteDeveloperToProject,
            ),
        )

        websiteManagerRole.associatedActivities.addAll(
            listOf(
                websiteManagerToProject,
            ),
        )

        websiteDeveloperRole.associatedActivities.addAll(
            listOf(
                websiteDeveloperToProject,
            ),
        )

        Assertions.assertEquals(
            Permissions(
                listOf(
                    Permission.VIEW_ACCOUNT,
                    Permission.VIEW_ACTIVITY,
                    Permission.EDIT_SETTINGS,
                    Permission.DELETE_ACTIVITY,
                    Permission.EDIT_ACTIVITY,
                ),
            ),
            managerAccount.getEffectivePermissionsForActivity(websiteProject),
        )

        Assertions.assertEquals(
            Permissions(
                listOf(
                    Permission.VIEW_ACCOUNT,
                    Permission.VIEW_ACTIVITY,
                    Permission.EDIT_ACTIVITY,
                ),
            ),
            developerAccount.getEffectivePermissionsForActivity(websiteProject),
        )

        Assertions.assertEquals(
            Permissions(
                listOf(
                    Permission.VIEW_ACCOUNT,
                    Permission.VIEW_ACTIVITY,
                ),
            ),
            memberAccount.getEffectivePermissionsForActivity(websiteProject),
        )
    }
}
