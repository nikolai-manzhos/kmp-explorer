package com.kmp.explorer.external

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

enum class SourceSetType(val value: String) {
    MAIN(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME),
    TEST(KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME)
}