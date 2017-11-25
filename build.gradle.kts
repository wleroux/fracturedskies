repositories {
  maven("http://dl.bintray.com/kotlin/kotlin-eap-1.2")
  maven("https://oss.sonatype.org/content/repositories/snapshots/")
}
plugins {
  kotlin("jvm") version "1.2.0-rc-84"
  idea
  application
}

kotlin {
  experimental.coroutines = org.jetbrains.kotlin.gradle.dsl.Coroutines.ENABLE
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions {
    jvmTarget = "1.8"
  }
}

application {
  mainClassName = "com.fracturedskies.MainKt"
}

repositories {
  jcenter()
}

val lwjglVersion = "3.1.4-SNAPSHOT"
val lwjglNatives = when (org.gradle.internal.os.OperatingSystem.current()) {
  org.gradle.internal.os.OperatingSystem.WINDOWS -> "natives-windows"
  org.gradle.internal.os.OperatingSystem.LINUX -> "natives-linux"
  org.gradle.internal.os.OperatingSystem.MAC_OS -> "natives-macos"
  else -> "natives-windows"
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.2.0-rc-39")
  implementation("org.jetbrains.kotlin:kotlin-reflect:1.2.0-rc-39")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:0.19.3")
  implementation("org.lwjgl:lwjgl:$lwjglVersion")
  implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
  implementation("org.lwjgl:lwjgl-jemalloc:$lwjglVersion")
  implementation("org.lwjgl:lwjgl-openal:$lwjglVersion")
  implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")
  implementation("org.lwjgl:lwjgl-stb:$lwjglVersion")

  implementation("org.lwjgl:lwjgl:$lwjglVersion:$lwjglNatives")
  implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion:$lwjglNatives")
  implementation("org.lwjgl:lwjgl-jemalloc:$lwjglVersion:$lwjglNatives")
  implementation("org.lwjgl:lwjgl-openal:$lwjglVersion:$lwjglNatives")
  implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion:$lwjglNatives")
  implementation("org.lwjgl:lwjgl-stb:$lwjglVersion:$lwjglNatives")

  testCompile("junit:junit:4.11")
  testCompile("org.jetbrains.kotlin:kotlin-test-junit")
}