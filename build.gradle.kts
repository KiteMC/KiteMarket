allprojects {
    group = "com.kitemc"; version = "1.0.0"
    repositories { mavenCentral(); maven("https://repo.papermc.io/repository/maven-public/") }
}
subprojects {
    apply(plugin = "java-library")
    extensions.configure<JavaPluginExtension> { toolchain.languageVersion.set(JavaLanguageVersion.of(21)) }
    tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8"; options.release.set(11) }
}
