package io.github.costaalex.workoutrelay

import config.BaseSpringITConfig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

class ApplicationContextIT : BaseSpringITConfig() {
    @Autowired
    lateinit var context: ApplicationContext

    @Test
    fun `should start context`() {
        Assertions.assertNotNull(context)
    }
}
