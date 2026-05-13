plugins {
    id("java")
    id("application")
}

group = "task.trak"
version = file("VERSION").readText().trim()

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.apache.parquet:parquet-avro:1.15.1")
    implementation("org.apache.hadoop:hadoop-common:3.4.1") {
        exclude(group = "org.slf4j")
        exclude(group = "dnsjava")
    }
    implementation(platform("org.mongodb:mongodb-driver-bom:5.6.5"))
    implementation("org.mongodb:mongodb-driver-sync")
    implementation("org.apache.hadoop:hadoop-mapreduce-client-core:3.4.1") {
        exclude(group = "org.slf4j")
    }
    implementation("org.slf4j:slf4j-nop:2.0.16")
    implementation("org.apache.avro:avro:1.12.1")
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("io.cucumber:cucumber-java:7.34.3")
    testImplementation("io.cucumber:cucumber-junit:7.34.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("task.trak.Main")
}

fun createFatJar(name: String, mainClassName: String): TaskProvider<Jar> {
    return tasks.register<Jar>(name) {
        archiveBaseName.set(name)
        manifest { attributes["Main-Class"] = mainClassName }
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
            exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
        }
        with(tasks.jar.get())
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

// 3 separate executables
val serverJar = createFatJar("trak-server", "task.trak.app.server.ServerMain")
val cliJar = createFatJar("trak-cli", "task.trak.app.client.cli.CLIMain")
val guiJar = createFatJar("trak-gui", "task.trak.app.client.gui.GUIMain")

// Default jar still uses unified Main
tasks.jar {
    manifest {
        attributes["Main-Class"] = "task.trak.Main"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Build all jars
tasks.register("allJars") {
    dependsOn(serverJar, cliJar, guiJar)
}

tasks.test {
    systemProperty("cucumber.features", System.getProperty("cucumber.features"))
    systemProperty("cucumber.filter.tags", System.getProperty("cucumber.filter.tags"))
    systemProperty("cucumber.filter.name", System.getProperty("cucumber.filter.name"))
    systemProperty("cucumber.plugin", System.getProperty("cucumber.plugin"))
}
