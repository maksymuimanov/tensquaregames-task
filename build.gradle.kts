plugins {
    id("java")
    id("application")
}

group = "io.maksymuimanov.task"
version = "1.0.0"


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.netty:netty-all:4.1.115.Final")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.1")

    implementation("io.lettuce:lettuce-core:6.3.2.RELEASE")

    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.13")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    implementation("org.jspecify:jspecify:1.0.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.awaitility:awaitility:4.2.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Tar>("distTarGz") {
    archiveClassifier.set("sources")
    compression = Compression.GZIP
    from(projectDir) {
        include("src/**", "build.gradle.kts", "settings.gradle.kts", "gradlew", "gradlew.bat", "gradle/**", "README.md")
        exclude("**/build/**", "**/.gradle/**", ".idea/**", "**/.git/**")
    }
}

application {
    mainClass = "io.maksymuimanov.task.Main"
}