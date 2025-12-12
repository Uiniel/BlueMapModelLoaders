plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.7"
    id("maven-publish")
}

group = "me.owies"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.bluecolored.de/releases")
    // maven("https://repo.bluecolored.de/snapshots")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("com.technicjelle:BMUtils:4.3.1")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    compileOnly("de.bluecolored:bluemap-core:5.15")
    compileOnly("de.bluecolored:bluemap-common:5.15")
    compileOnly("de.bluecolored:bluemap-api:2.7.7")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

tasks.shadowJar {
    relocate("com.technicjelle.BMUtils", "${project.group}.${project.name}.BMUtils")
    relocate("com.github.benmanes.caffeine", "${project.group}.${project.name}.caffeine")
    relocate("org.checkerframework", "${project.group}.${project.name}.checkerframework")
    relocate("com.google.errorprone", "${project.group}.${project.name}.errorprone")
    relocate("org.intellij", "${project.group}.${project.name}.intellij")
    relocate("org.jetbrains", "${project.group}.${project.name}.jetbrains")
    archiveFileName = "${project.name}-${project.version}.jar"
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    filesMatching("bluemap.addon.json") {
        expand(project.properties)
    }
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}