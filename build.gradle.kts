plugins {
  kotlin("jvm") version "1.1.51"
  idea
  application
}

application {
  mainClassName = "com.fracturedskies.MainKt"
}

repositories {
  jcenter()
}


val lwjglVersion = "3.1.3"
val lwjglNatives = when (org.gradle.internal.os.OperatingSystem.current()) {
  org.gradle.internal.os.OperatingSystem.WINDOWS -> "natives-windows"
  org.gradle.internal.os.OperatingSystem.LINUX -> "natives-linux"
  org.gradle.internal.os.OperatingSystem.MAC_OS -> "natives-macos"
  else -> "natives-windows"
}

dependencies {
  compile("org.jetbrains.kotlin:kotlin-stdlib-jre8")
  compile("org.jetbrains.kotlin:kotlin-reflect")
  compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:0.19.3")
  compile("org.lwjgl:lwjgl:$lwjglVersion")
  compile("org.lwjgl:lwjgl-glfw:$lwjglVersion")
  compile("org.lwjgl:lwjgl-jemalloc:$lwjglVersion")
  compile("org.lwjgl:lwjgl-openal:$lwjglVersion")
  compile("org.lwjgl:lwjgl-opengl:$lwjglVersion")
  compile("org.lwjgl:lwjgl-stb:$lwjglVersion")

  runtime("org.lwjgl:lwjgl:$lwjglVersion:$lwjglNatives")
  runtime("org.lwjgl:lwjgl-glfw:$lwjglVersion:$lwjglNatives")
  runtime("org.lwjgl:lwjgl-jemalloc:$lwjglVersion:$lwjglNatives")
  runtime("org.lwjgl:lwjgl-openal:$lwjglVersion:$lwjglNatives")
  runtime("org.lwjgl:lwjgl-opengl:$lwjglVersion:$lwjglNatives")
  runtime("org.lwjgl:lwjgl-stb:$lwjglVersion:$lwjglNatives")
}