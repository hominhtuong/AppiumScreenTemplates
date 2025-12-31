plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.7.1"
}

group = "com.mitu.appium"
version = "2.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create("IC", "2025.1.4.1")
        bundledPlugin("com.intellij.java")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        name = "MiTu Appium Templates"
        version = project.version.toString()

        vendor {
            name = "MiTu"
            email = "support@mituultra.com"
        }
        ideaVersion {
            sinceBuild = "241"
        }

        description = """
            This plugin helps Appium automation engineers quickly generate screen and test files based on predefined templates.
By entering a screen name, the plugin automatically creates corresponding screen and test classes, ensuring consistent structure and faster test development.
        """.trimIndent()

        changeNotes = """
                    <li>Add MobileFindBy to replace AndroidFindBy and iOSXCUITFindBy for simpler locator definition</li> 
        """.trimIndent()
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
