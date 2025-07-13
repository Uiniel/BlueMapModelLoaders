plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.7"
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
    maven("https://repo.bluecolored.de/snapshots")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("com.technicjelle:BMUtils:4.3.1")

    compileOnly("de.bluecolored:bluemap-core:5.9-feat.atlases-13")
    compileOnly("de.bluecolored:bluemap-common:5.7")
    compileOnly("de.bluecolored:bluemap-api:2.7.4")
    compileOnly("me.owies:bluemapmodelloaders-common:0.2.2")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

tasks.shadowJar {
    relocate("com.technicjelle.BMUtils", "${project.group}.${project.name}.BMUtils")
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