plugins {
    kotlin("jvm") version "2.0.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

val ktorVersion = "2.3.5" // проверяй актуальную версию
val logbackVersion = "1.4.11"

dependencies {
    // Ktor сервер + Netty
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")

    // Content negotiation (JSON)
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")

    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // Корутины
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Логирование
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // H2 база (если нужна)
    implementation("com.h2database:h2:2.2.224")


    implementation("io.ktor:ktor-serialization-gson:$ktorVersion")
}


