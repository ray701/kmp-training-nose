plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlinSerialization)
    application
}

application {
    mainClass.set("com.example.server.ApplicationKt")
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
}
