package pt.up.fe.ni.website.backend.service.activity

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import pt.up.fe.ni.website.backend.dto.entity.ActivityDto
import pt.up.fe.ni.website.backend.model.Activity
import pt.up.fe.ni.website.backend.repository.ActivityRepository
import pt.up.fe.ni.website.backend.service.AccountService
import pt.up.fe.ni.website.backend.service.ErrorMessages
import pt.up.fe.ni.website.backend.service.upload.FileUploader

@Service
abstract class AbstractActivityService<T : Activity>(
    protected val repository: ActivityRepository<T>,
    protected val accountService: AccountService,
    protected val fileUploader: FileUploader
) {

    fun getActivityById(id: Long): T =
        repository.findByIdOrNull(id)
            ?: throw NoSuchElementException(ErrorMessages.activityNotFound(id))

    fun <U : ActivityDto<T>> createActivity(dto: U, imageFolder: String): T {
        repository.findBySlug(dto.slug)?.let {
            throw IllegalArgumentException(ErrorMessages.slugAlreadyExists)
        }

        dto.imageFile?.let {
            val fileName = fileUploader.buildFileName(it, dto.title)
            dto.image = fileUploader.uploadImage(imageFolder, fileName, it.bytes)
        }

        val activity = dto.create()

        dto.teamMembersIds?.forEach {
            val account = accountService.getAccountById(it)
            activity.teamMembers.add(account)
        }

        return repository.save(activity)
    }

    fun <U : ActivityDto<T>> updateActivityById(activity: T, dto: U, imageFolder: String): T {
        if (dto.slug != activity.slug) {
            repository.findBySlug(dto.slug)?.let {
                throw IllegalArgumentException(ErrorMessages.slugAlreadyExists)
            }
        }

        val imageFile = dto.imageFile
        if (imageFile == null) {
            dto.image = activity.image
        } else {
            val fileName = fileUploader.buildFileName(imageFile, dto.title)
            dto.image = fileUploader.uploadImage(imageFolder, fileName, imageFile.bytes)
        }

        val newActivity = dto.update(activity)
        newActivity.apply {
            teamMembers.clear()
            dto.teamMembersIds?.forEach {
                val account = accountService.getAccountById(it)
                teamMembers.add(account)
            }
        }
        return repository.save(newActivity)
    }

    fun addTeamMemberById(idActivity: Long, idAccount: Long): T {
        val activity = getActivityById(idActivity)
        val account = accountService.getAccountById(idAccount)
        activity.teamMembers.add(account)
        return repository.save(activity)
    }

    fun removeTeamMemberById(idActivity: Long, idAccount: Long): T {
        val activity = getActivityById(idActivity)
        if (!accountService.doesAccountExist(idAccount)) {
            throw NoSuchElementException(ErrorMessages.accountNotFound(idAccount))
        }
        activity.teamMembers.removeIf { it.id == idAccount }
        return repository.save(activity)
    }

    fun addGalleryImage(activityId: Long, image: MultipartFile, imageFolder: String): Activity {
        val activity = getActivityById(activityId)

        val fileName = fileUploader.buildFileName(image, activity.title)
        val imageName = fileUploader.uploadImage(imageFolder + "/gallery", fileName, image.bytes)

        activity.gallery.add(imageName)

        return repository.save(activity)
    }

    fun removeGalleryImage(activityId: Long, imageName: String): Activity {
        val activity = getActivityById(activityId)

        val imageRemoved = activity.gallery.remove(imageName)

        if (!imageRemoved) {
            throw NoSuchElementException(ErrorMessages.fileNotFound(imageName))
        }

        return repository.save(activity)
    }
}
