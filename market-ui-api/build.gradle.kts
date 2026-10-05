plugins { `java-library`; `maven-publish` }

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    testImplementation("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    constraints {
        compileOnly("com.google.code.gson:gson:2.11.0")
        testImplementation("com.google.code.gson:gson:2.11.0")
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}
tasks.withType<JavaCompile>().configureEach { options.release.set(11) }
tasks.withType<Jar>().configureEach {
    archiveBaseName.set("KiteMarket-UI-API")
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    from("LICENSE") { into("META-INF") }
}
tasks.named<Jar>("jar") {
    manifest.attributes["Automatic-Module-Name"] = "com.kitemc.market.ui.api"
}
tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).noTimestamp(true)
}

publishing {
    publications {
        create<MavenPublication>("sdk") {
            from(components["java"])
            artifactId = "kitemarket-ui-api"
            pom {
                name.set("KiteMarket UI API")
                description.set("Java 11 SDK for freely developed KiteMarket renderers and themes.")
                url.set("https://github.com/KiteMC/KiteMarket")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/license/mit/")
                    }
                }
                developers { developer { id.set("KiteMC"); name.set("KiteMC") } }
                scm {
                    connection.set("scm:git:https://github.com/KiteMC/KiteMarket.git")
                    developerConnection.set("scm:git:ssh://git@github.com/KiteMC/KiteMarket.git")
                    url.set("https://github.com/KiteMC/KiteMarket")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/kitemc/KiteMarket")
            credentials {
                username = providers.gradleProperty("gpr.user")
                    .orElse(providers.environmentVariable("GITHUB_ACTOR")).orNull
                password = providers.gradleProperty("gpr.key")
                    .orElse(providers.environmentVariable("GITHUB_TOKEN")).orNull
            }
        }
    }
}
