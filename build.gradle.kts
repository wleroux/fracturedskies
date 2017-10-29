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

dependencies {
  compile("org.jetbrains.kotlin:kotlin-stdlib-jre8")
  compile("org.jetbrains.kotlin:kotlin-reflect")
  compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:0.19.3")
}