plugins {
    `kotlin-dsl`
}

group = "com.seankenkeremath.uselessui.convention"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.vanniktech.maven.publish)
}

gradlePlugin {
    plugins {
        create("uselessUiLibMavenPublish") {
            id = "com.seankenkeremath.uselessui.convention.publish"
            implementationClass = "com.seankenkeremath.uselessui.convention.UselessUiLibMavenPublishConventionPlugin"
        }
    }
}