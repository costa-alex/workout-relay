package io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.configuration

import io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.TrainerRoadMemberDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    value = "TrainerRoadValidationApiClient",
    url = "\${app.trainer-road.api-url}",
    dismiss404 = true,
)
interface TrainerRoadValidationApiClient {
    @GetMapping("/app/api/member-info")
    fun getMember(@RequestHeader(HttpHeaders.COOKIE) cookie: String): TrainerRoadMemberDTO?
}
