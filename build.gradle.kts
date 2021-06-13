import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.4.32"
    kotlin("plugin.spring") version "1.4.32"
}

group = "cz.exposed.spring.failing.transactions"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    val springBootPlatform = platform("org.springframework.boot:spring-boot-dependencies:2.5.0")
    annotationProcessor(springBootPlatform)

    implementation(springBootPlatform)
    implementation("org.springframework.boot", "spring-boot-starter-web")

    implementation("org.postgresql", "postgresql")

    val exposedVersion = "0.32.1"
    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")


    implementation(platform("org.testcontainers:testcontainers-bom:1.15.2"))
    implementation("org.testcontainers:postgresql")
}

allprojects {
    apply(plugin = "java")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_11.majorVersion
        targetCompatibility = JavaVersion.VERSION_11.majorVersion
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}