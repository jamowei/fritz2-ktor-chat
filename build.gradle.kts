plugins {
    application
    id("dev.fritz2.fritz2-gradle") version "0.9"
    kotlin("multiplatform") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.30"
}

group = "dev.fritz2"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://oss.jfrog.org/artifactory/jfrog-dependencies")
    maven("https://dl.bintray.com/kotlin/kotlin-js-wrappers")
    maven("https://dl.bintray.com/kotlin/kotlinx")
    maven("https://dl.bintray.com/kotlin/ktor")
}

application {
    mainClassName = "app.backend.ServerKt"
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(IR) {
        browser {
            runTask {
                devServer = devServer?.copy(
                    port = 9000,
                    proxy = mapOf(
                        "/members" to "http://localhost:8080",
                        "/chat" to mapOf(
                            "target" to "ws://localhost:8080",
                            "ws" to true
                        )
                    )
                )
            }
        }
    }.binaries.executable()

    sourceSets {
        val ktorVersion = "1.4.2"
        val logbackVersion = "1.2.3"
        val serializationVersion = "1.0.1"
        val dateTimeVersion = "0.1.1"

        all {
            languageSettings.apply {
                useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation("dev.fritz2:components:0.9")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$dateTimeVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-websockets:$ktorVersion")
                implementation("ch.qos.logback:logback-classic:$logbackVersion")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("io.ktor:ktor-server-test-host:$ktorVersion")
            }
        }
        val jsMain by getting {
            dependencies {
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

tasks {
    getByName<ProcessResources>("jvmProcessResources") {
        dependsOn(getByName("jsBrowserProductionWebpack"))
        val jsBrowserProductionWebpack = getByName<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("jsBrowserProductionWebpack")
        from(File(jsBrowserProductionWebpack.destinationDirectory, jsBrowserProductionWebpack.outputFileName))
    }

    getByName<JavaExec>("run") {
        dependsOn(getByName<Jar>("jvmJar"))
        classpath(getByName<Jar>("jvmJar"))
        classpath(configurations.jvmRuntimeClasspath)
    }
}