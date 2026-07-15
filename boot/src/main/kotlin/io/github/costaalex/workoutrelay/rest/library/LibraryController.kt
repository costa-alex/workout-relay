package io.github.costaalex.workoutrelay.rest.library

import io.github.costaalex.workoutrelay.app.plan.CopyLibraryRequest
import io.github.costaalex.workoutrelay.app.plan.CopyPlanResponse
import io.github.costaalex.workoutrelay.app.plan.LibraryService
import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.librarycontainer.LibraryContainer
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class LibraryController(
    private val libraryService: LibraryService
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/api/library-container")
    fun getLibraryContainers(@RequestParam platform: Platform): List<LibraryContainer> {
        log.debug("Received request for getting library containers: {}", platform)
        return libraryService.findByPlatform(platform)
    }

    @PostMapping("/api/library-container/copy")
    fun copyLibraryContainer(@RequestBody request: CopyLibraryRequest): CopyPlanResponse {
        log.debug("Received request to copy the library container: {}", request)
        return libraryService.copyLibrary(request)
    }
}
