plugins {
  kotlin("jvm") version "1.9.0-Beta"
  kotlin("plugin.serialization") version "1.9.0-Beta"
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
}
