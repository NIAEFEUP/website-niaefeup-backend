package pt.up.fe.ni.website.backend.utils.annotations

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestExecutionListeners
import pt.up.fe.ni.website.backend.utils.listeners.DbCleanupListener

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase

@TestExecutionListeners(
    listeners = [DbCleanupListener::class],
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)

internal annotation class ControllerTest
