/**
 * Shared base configuration for `pklJavaDocs.gradle.kts` and `pklKotlinDocs.gradle.kts`.
 */
import dev.adamko.dokkatoo.dokka.parameters.VisibilityModifier

plugins {
  // Every project that generates API docs already applies the Kotlin plugin for testing.
  // Also applying it here helps with Dokkatoo.
  kotlin("jvm")
  id("dev.adamko.dokkatoo-html")
}

dokkatoo {
  pluginsConfiguration.html {
    footerMessage.set(
      "Copyright © 2024 Apple Inc. and the Pkl project authors. All rights reserved.")
  }
  dokkatooSourceSets {
    // For configuration options, see
    // https://kotlinlang.org/docs/dokka-gradle.html#source-set-configuration
    // or open class DokkaSourceSetSpec in IntelliJ.
    named("main") {
      jdkVersion.set(17) // link to official JDK 17 docs
      documentedVisibilities(VisibilityModifier.PUBLIC, VisibilityModifier.PROTECTED)
    }
  }
}
