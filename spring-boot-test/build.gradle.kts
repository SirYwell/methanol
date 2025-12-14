plugins {
  id("conventions.java-testing")
  id("conventions.static-analysis")
  id("conventions.coverage")
  alias(libs.plugins.spring.boot)
}

apply(plugin = libs.plugins.spring.dependency.management.get().pluginId)

dependencies {
  implementation(project(":methanol"))
  implementation(project(":methanol-jackson"))
  implementation(project(":methanol-testing"))
  implementation(libs.mockwebserver)

  // Must explicitly declare okhttp dep to avoid a weird NoClassDefFoundError due to an old okhttp
  // version spring-boot puts in the boot jar.
  implementation(libs.okhttp)
  implementation(libs.spring.boot.starter.web)
  implementation(libs.autoservice.annotations)
  annotationProcessor(libs.autoservice.annprocess)
}

// Make sure we only run this in Java targetRelease+ setups.
val targetRelease = 25

tasks.withType<JavaCompile> {
  onlyIf {
    java.toolchain.languageVersion.get().asInt() >= targetRelease
  }
  options.release = targetRelease // Override to look for the correct dependencies.
}

tasks.withType<Test> {
  onlyIf {
    java.toolchain.languageVersion.get().asInt() >= targetRelease
  }
}

tasks.bootJar {
  onlyIf {
    java.toolchain.languageVersion.get().asInt() >= targetRelease
  }
}

tasks.test {
  dependsOn(tasks.bootJar)
  doFirst {
    systemProperty(
      "com.github.mizosoft.methanol.springboot.test.bootJarPath",
      tasks.bootJar.flatMap { it.archiveFile }.get()
    )
  }
}
