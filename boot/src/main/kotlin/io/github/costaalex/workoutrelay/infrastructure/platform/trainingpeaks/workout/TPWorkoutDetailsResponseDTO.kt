package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout

class TPWorkoutDetailsResponseDTO(
    val workoutId: String,
    val attachmentFileInfos: List<FileInfoDTO> = listOf(),
) {
    class FileInfoDTO(
        val fileId: String,
        val fileName: String,
        val contentType: String,
    )
}
