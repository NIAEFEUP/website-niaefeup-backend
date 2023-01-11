package pt.up.fe.ni.website.backend.utils.annotations

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.springframework.test.context.NestedTestConfiguration

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Nested
@NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.OVERRIDE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal annotation class NestedTest
