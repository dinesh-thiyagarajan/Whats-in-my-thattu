// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
    }

    dependencies {

    }
}



plugins {
    id("com.android.application") version "8.1.0-rc01" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
    alias(libs.plugins.com.android.library) apply false
    alias(libs.plugins.hilt) apply false
    id("com.diffplug.spotless") version "6.9.0" apply false
}


subprojects {
    afterEvaluate {

    }
}