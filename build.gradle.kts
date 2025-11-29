plugins {
    java
    id("com.diffplug.spotless") version "6.25.0"
    id("checkstyle")
}

group = "io.cto.loomevent"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
}

tasks.test {
    useJUnitPlatform()
}

spotless {
    java {
        googleJavaFormat("1.19.2")
    }
}

checkstyle {
    toolVersion = "10.14.0"
}

configurations.checkstyle {
    resolutionStrategy {
        capabilitiesResolution.withCapability("com.google.collections:google-collections") {
            select("com.google.guava:guava:33.0.0-jre")
        }
    }
}
