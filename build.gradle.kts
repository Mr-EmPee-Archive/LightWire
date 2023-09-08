plugins {
    id("io.freefair.lombok") version "8.3"
    id("maven-publish")
    id("java")
}

group = "mr.empee"
version = System.getenv("CI_COMMIT_TAG") ?: "develop"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            var projectId = System.getenv("CI_PROJECT_ID")
            url = uri("https://gitlab.com/api/v4/projects/$projectId/packages/maven")
            credentials(HttpHeaderCredentials::class) {
                name = "Job-Token"
                value = System.getenv("CI_JOB_TOKEN")
            }

            authentication {
                create("header", HttpHeaderAuthentication::class)
            }
        }
    }
}