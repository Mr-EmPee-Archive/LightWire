plugins {
    id("io.freefair.lombok") version "8.3"
    id("maven-publish")
    id("java")
}

group = "mr.empee"
version = System.getenv("GITHUB_REF_NAME") ?: "develop"

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

publishing {
    repositories {
        maven {
            name = "GitHubPackages"

            var repo = System.getenv("GITHUB_REPOSITORY")
            url = uri("https://maven.pkg.github.com/${repo}")

            credentials {
                username = System.getenv("USERNAME")
                password = System.getenv("TOKEN")
            }
        }
    }
    
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
            artifactId = "lightwire"
        }
    }
}