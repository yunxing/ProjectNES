import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // kotlin("jvm") version "1.4.32"
    kotlin("js") version "1.4.32"
}

repositories {
    mavenCentral()
}

dependencies {
//    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

//    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

//    implementation("com.google.guava:guava:30.0-jre")

    testImplementation(kotlin("test-js"))

    //testImplementation("org.jetbrains.kotlin:kotlin-test")

    //testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    // implementation(kotlin("stdlib-jdk8"))
}


kotlin {
    js {
        browser {
            webpackTask {
                outputFileName = "cpu.js"
                output.libraryTarget = "commonjs2"
            }
        }
        nodejs()
        binaries.executable()
    }
}

tasks.withType(KotlinCompile::class).all  {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.ExperimentalUnsignedTypes",
            "-Xopt-in=kotlin.ExperimentalStdlibApi",
        )
    }
}
