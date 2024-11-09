plugins {
    kotlin("jvm")
    id("maven-publish")
}

group = "com.gtech"
version = "1.0.6"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.github.java-native:jssc:2.9.6")
    implementation("org.slf4j:slf4j-simple:2.1.0-alpha1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = project.name
            version = version
        }
    }
}
