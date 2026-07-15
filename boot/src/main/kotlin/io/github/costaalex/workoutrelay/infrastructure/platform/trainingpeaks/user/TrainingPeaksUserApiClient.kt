package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.user

import io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.TrainingPeaksApiClientConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping

@FeignClient(
    value = "TrainingPeaksUserApiClient",
    url = "\${app.training-peaks.api-url}",
    dismiss404 = true,
    primary = false,
    configuration = [TrainingPeaksApiClientConfig::class]
)
interface TrainingPeaksUserApiClient {
    @GetMapping("/users/v3/user")
    fun getUser(): TrainingPeaksUserDTO
}
