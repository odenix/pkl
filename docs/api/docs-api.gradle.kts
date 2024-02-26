plugins {
  // The Dokkatoo Gradle plugin was merged into the Kotlin codebase 
  // in October 2023. It is set to replace the legacy Dokka Gradle plugin.
  // https://github.com/adamko-dev/dokkatoo
  // The underlying documentation engine is Kotlin's Dokka.
  // Dokka ingests both Javadoc and KDoc.
  // https://kotlinlang.org/docs/dokka-introduction.html
  id("dev.adamko.dokkatoo-html")
}

// The subprojects whose Java and Kotlin code should be included in API docs.
// Each listed subproject must also apply `pklJavaDocs.gradle.kts`
// or `pklKotlinDocs.gradle.kts` and must list its API packages in that file.
dependencies {
  dokkatoo(projects.pklCli)
  dokkatoo(projects.pklCodegenJava)
  dokkatoo(projects.pklCodegenKotlin)
  dokkatoo(projects.pklCommonsCli)
  dokkatoo(projects.pklConfigJava)
  dokkatoo(projects.pklConfigKotlin)
  dokkatoo(projects.pklCore)
  dokkatoo(projects.pklDoc)
  dokkatoo(projects.pklExecutor)
  dokkatoo(projects.pklGradle)
}

dokkatoo {
  moduleName.set("Pkl")
  
  //https://kotlinlang.org/docs/dokka-html.html#configuration-options
  pluginsConfiguration.html {
    // Overwrite default logo by name.
    // For greater customization, overwrite `logo-styles.css`.
    // https://kotlin.github.io/dokka/1.4.20/user_guide/base-specific/frontend/
    customAssets.from("logo-icon.svg")
    footerMessage.set(
      "Copyright © 2024 Apple Inc. and the Pkl project authors. All rights reserved.")
  }
  dokkaGeneratorIsolation = ProcessIsolation {
    // Dokka needs a lot of memory
    maxHeapSize = "1g"
  }
}

tasks.register("apiDocs") {
  dependsOn(tasks.dokkatooGenerate)
  doLast {
    // The main page of multi-module docs cannot 
    // currently be customized via config options.
    // But we can do some string replacement.
    val mainPage = file("build/dokka/html/index.html")
    val text = mainPage.readText()
    val modified = text
      .replaceFirst(
        """<title>All modules</title>""",
        """<title>Pkl API Docs</title>""")
      .replaceFirst(
        """<h2 class="">All modules:</h2>""",
        """<h2 class="">Pkl API Docs</h2>""")
    mainPage.writeText(modified)
    println("""
      Generated API docs at: $rootDir/docs/api/build/dokka/html/index.html
      To preview docs, open the above file on a local HTTP server, for example with IntelliJ.
    """.trimIndent())
  }
}
