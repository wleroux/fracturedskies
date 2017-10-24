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
  compile(kotlin("stdlib-jre8"))
}