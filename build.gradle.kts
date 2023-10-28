plugins {
    id("org.gradle.java-library")
    id("io.freefair.lombok") version "6.6.3"
}

group = "mr.empee"
version = "develop"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.classgraph:classgraph:4.8.162")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
}

tasks {
    test {
        useJUnitPlatform()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        withSourcesJar()
        withJavadocJar()

        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}