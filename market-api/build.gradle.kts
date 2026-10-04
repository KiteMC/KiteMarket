plugins { `java-library` }

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
}

java {
    withSourcesJar()
    withJavadocJar()
}
tasks.withType<JavaCompile>().configureEach { options.release.set(11) }
tasks.withType<Jar>().configureEach {
    archiveBaseName.set("KiteMarket-API")
    from("LICENSE") { into("META-INF") }
    manifest.attributes["Automatic-Module-Name"] = "com.kitemc.market.api"
}
