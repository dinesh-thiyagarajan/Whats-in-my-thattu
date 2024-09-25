// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.ktlint.gradle)
        classpath(libs.google.services)
        classpath(libs.firebase.crashlytics.gradle)
    }
}



plugins {
    id("com.android.application") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    alias(libs.plugins.com.android.library) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ktlint) apply false
    id("com.google.firebase.crashlytics") version "3.0.1" apply false
}


// Apply the ktlint plugin and configuration to all submodules
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    // Ktlint configuration
    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("0.48.1")
        android.set(true)
        outputColorName.set("RED")
        // Specify to include .kts files
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.JSON)
        }
    }
}