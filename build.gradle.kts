plugins {
    id("io.freefair.lombok") version "8.3"
    id("maven-publish")
    id("java")
}

group = "mr.empee"
version = "develop"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("io.github.classgraph:classgraph:4.8.162")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
}

tasks.test {
    useJUnitPlatform()
}