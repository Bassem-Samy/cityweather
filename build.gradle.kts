plugins {
    id("com.android.application") version "7.3.1" apply false
    id("com.android.library") version "7.3.1" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libraries.plugins.gradle.ktlint) apply false
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libraries.plugins.jetbrains.kotlin.serialization) apply false
}
