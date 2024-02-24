plugins {
  pklAllProjects
  pklJavaLibrary
  pklGradlePluginTest

  `java-gradle-plugin`
  `maven-publish`
  pklPublishLibrary
  signing
}

dependencies {
  // Declare a `compileOnly` dependency on `projects.pklTools`
  // to ensure correct code navigation in IntelliJ.
  compileOnly(projects.pklTools)

  // Declare a `runtimeOnly` dependency on `project(":pkl-tools", "fatJar")`
  // to ensure that the published plugin 
  // (and also plugin tests, see the generated `plugin-under-test-metadata.properties`) 
  // only depends on the pkl-tools shaded fat JAR.
  // This avoids dependency version conflicts with other Gradle plugins.
  //
  // Hide this dependency from IntelliJ 
  // to prevent IntelliJ from reindexing the pkl-tools fat JAR after every build.
  // (IntelliJ gets everything it needs from the `compileOnly` dependency.)
  //
  // To debug shaded code in IntelliJ, temporarily remove the conditional.
  if (System.getProperty("idea.sync.active") == null) {
    runtimeOnly(project(":pkl-tools", "fatJar"))
  }

  testImplementation(projects.pklCommonsTest)
}

publishing {
  publications {
    withType<MavenPublication>().configureEach {
      pom {
        name.set("pkl-gradle plugin")
        url.set("https://github.com/apple/pkl/tree/main/pkl-gradle")
        description.set("Gradle plugin for the Pkl configuration language.")
      }
    }
  }
}

gradlePlugin {
  plugins {
    create("pkl") {
      id = "org.pkl-lang"
      implementationClass = "org.pkl.gradle.PklPlugin"
      displayName = "pkl-gradle"
      description = "Gradle plugin for interacting with Pkl"
    }
  }
}

gradlePluginTests {
  // keep in sync with `PklPlugin.MIN_GRADLE_VERSION`
  minGradleVersion = GradleVersion.version("7.2")
  maxGradleVersion = GradleVersion.version("8.99")
  skippedGradleVersions = listOf()
}

signing {
  publishing.publications.withType(MavenPublication::class.java).configureEach {
    if (name != "library") {
      sign(this)
    }
  }
}

tasks.compileTestKotlin {
  // Work around a conflict between Pkl's and Gradle's
  // Kotlin dependencies on the test compile class path.
  //
  // My preferred solution would be to clean up the test 
  // compile class path to no longer contain a Gradle distribution. 
  // However, my Gradle knowledge proved insufficient to accomplish this.
  //
  // Another potential workaround is to port plugin tests to Java.
  // (If the plugin was written in Kotlin, its compilation would 
  // currently fail with the same error.)
  kotlinOptions {
    freeCompilerArgs += "-Xskip-metadata-version-check"
  }
}

// for debugging
val printCompileClasspath by tasks.registering {
  doLast { println(configurations.compileClasspath.get().files) }
}

// for debugging
val printTestCompileClasspath by tasks.registering {
  doLast { println(configurations.testCompileClasspath.get().files) }
}
