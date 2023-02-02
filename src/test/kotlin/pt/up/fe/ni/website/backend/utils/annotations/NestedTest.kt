package pt.up.fe.ni.website.backend.utils.annotations

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Nested
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal annotation class NestedTest
