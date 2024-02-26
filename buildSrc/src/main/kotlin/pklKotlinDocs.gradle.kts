plugins {
  id("pklApiDocs")
}

dokkatoo {
  dokkatooSourceSets {
    named("main") {
      // The following packages are considered public Kotlin APIs.
      sourceRoots = fileTree("src/main/kotlin/org/pkl") {
        include("cli/*.kt")
        include("codegen/kotlin/*.kt")
        include("codegen/java/*.kt")
        include("commons/cli/*.kt")
        include("config/kotlin/*.kt")
        include("doc/*.kt")
      }
    }
  }
}
