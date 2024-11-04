import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "com.gtech"
version = "1.0.5"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {

    implementation(compose.desktop.currentOs) {
        exclude("org.jetbrains.compose.material")
    }
    implementation(compose.material3)
    implementation(project(":KSerial"))

}

compose.desktop {
    application {
        nativeDistributions {
            modules("java.compiler", "java.instrument" , "java.sql", "jdk.unsupported")
            modules("java.naming", "java.management", "java.xml", "jdk.management")
            buildTypes {
                release {
                    proguard {
                        configurationFiles.from(project.file("proguard-rules.pro"))
                        isEnabled.set(false)
                        obfuscate.set(false)
                        optimize.set(true)
                    }
                }
            }
            targetFormats(TargetFormat.Msi)
            mainClass = "MainKt"
            packageName = "KSerial"
            packageVersion = version.toString()
        }
    }
}