/*
 *
 * Copyright (C) 2025 mintychochip
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

plugins {
    id("java-library")
    id("maven-publish")
    id("com.gradleup.shadow") version "8.3.5"
    id("io.papermc.paperweight.userdev") version "1.7.7"
}

group = "org.aincraft"
version = "1.0-SNAPSHOT"
allprojects {
    plugins.apply("java-library")
    plugins.apply("maven-publish")
    plugins.apply("com.gradleup.shadow")

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven {
            name="papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
    }
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        withSourcesJar()
    }
    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.release = 21
            options.compilerArgs.add("-Xlint:none")
        }
        jar {
            archiveClassifier.set("noshade")
        }
        shadowJar {
            archiveClassifier.set("")
            archiveFileName.set("${project.property("artifactName")}-${project.version}.jar")
            destinationDirectory.set(file("C:\\Users\\justi\\Desktop\\paper\\plugins"))
            relocate("dev.triumphteam.gui","org.aincraft.gui")
        }
        build {
            dependsOn(shadowJar)
        }
        test {
            useJUnitPlatform()
        }
        processResources {
            filesMatching("plugin.yml") {
                expand(
                    "version" to project.version,
                    "name" to project.findProperty("artifactName")
                )
            }
        }
    }


}
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION



publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.aincraft"
            artifactId = "${project.property("artifactName")}".lowercase()
            version = "${project.version}"

            from(components["java"])
        }
    }
}
dependencies {
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("com.github.ben-manes.caffeine:caffeine:2.9.0")
    //implementation("org.fusesource.jansi:jansi:2.4.0")
    implementation("com.google.inject:guice:7.0.0")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("dev.triumphteam:triumph-gui:3.1.11")
    compileOnly(libs.adventureApi)
    compileOnly(libs.adventureMinimessage)
    compileOnly(libs.adventureLegacy)
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:23.0.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.0.0")
}

