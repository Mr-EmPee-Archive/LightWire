plugins {
    id("java")
    id("io.freefair.lombok") version "8.3"
}

group = "mr.empee.lightwire"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.bcel:bcel:6.7.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
}

tasks.test {
    useJUnitPlatform()
}