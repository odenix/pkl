plugins {
  id("pklApiDocs")
}

dependencies {
  // display Java signatures for Java APIs
  dokkatooPluginHtml("org.jetbrains.dokka:kotlin-as-java-plugin:1.9.10")
}

dokkatoo {
  dokkatooSourceSets {
    named("main") {
      // The following packages are considered public Java APIs.
      sourceRoots = fileTree("src/main/java/org/pkl") {
        include("config/java/*.java")
        include("config/java/mapper/*.java")
        include("core/*.java")
        include("core/http/*.java")
        include("core/module/*.java")
        include("core/packages/*.java")
        include("core/project/*.java")
        include("core/settings/*.java")
        include("executor/*.java")
        include("gradle/*.java")
        include("gradle/spec/*.java")
        include("gradle/task/*.java")
      }
    }
  }
}
