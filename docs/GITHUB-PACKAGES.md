# GitHub Packages 开发包

KiteMarket 的两套独立 Java 11／MIT SDK 提供 Maven 依赖；闭源主插件不作为 Maven 开发包发布。

| 开发包 | Maven 坐标 |
| --- | --- |
| 只读查询与成交通知 | `com.kitemc:kitemarket-api:1.0.0` |
| 页面呈现与 IA 界面扩展 | `com.kitemc:kitemarket-ui-api:1.0.0` |

仓库地址为 `https://maven.pkg.github.com/kitemc/KiteMarket`。两个开发包都附带 sources、Javadoc 和 MIT 许可证，选择与主插件匹配的版本。运行包、语言／配置包、可运行示例和 SDK 直接下载仍保留在 [GitHub Releases](https://github.com/KiteMC/KiteMarket/releases)。

## 认证

GitHub 的公开 Maven 包也需要认证。本地下载使用 [classic PAT](https://github.com/settings/tokens/new?scopes=read:packages&description=KiteMarket%20SDK%20read)，仅授予 `read:packages`，用户名填自己的 GitHub 用户名；fine-grained PAT 不适用于 Maven Registry。将令牌放在用户目录的 `~/.gradle/gradle.properties`，不要提交到项目仓库：

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_CLASSIC_PAT
```

也可用环境变量 `GITHUB_ACTOR` 和 `GITHUB_TOKEN`。不希望配置 Maven 认证时，从 Release 下载 SDK，继续用 `compileOnly(files("libs/KiteMarket-API-1.0.0.jar"))`。

## Gradle Kotlin DSL

```kotlin
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven {
        name = "KiteMarketPackages"
        url = uri("https://maven.pkg.github.com/kitemc/KiteMarket")
        content { includeGroup("com.kitemc") }
        credentials {
            username = providers.gradleProperty("gpr.user")
                .orElse(providers.environmentVariable("GITHUB_ACTOR")).orNull
            password = providers.gradleProperty("gpr.key")
                .orElse(providers.environmentVariable("GITHUB_TOKEN")).orNull
        }
    }
}
dependencies {
    compileOnly("com.kitemc:kitemarket-api:1.0.0")
    // 需要界面扩展时再加入：
    compileOnly("com.kitemc:kitemarket-ui-api:1.0.0")
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
}
```

公开 SDK 编译使用 JDK 21，字节码为 Java 11；IA 适配示例仍需要 Java 21。SDK 不传递 Paper、ItemsAdder 或闭源核心依赖，开发者按自己的平台需求添加公开 API。

只使用 `compileOnly`，不要把 SDK 打包、shade 或重定位进第三方插件。主插件提供唯一运行时接口类。Maven 对应使用 `provided`：

```xml
<repository>
  <id>github-kitemarket</id>
  <url>https://maven.pkg.github.com/kitemc/KiteMarket</url>
</repository>

<dependency>
  <groupId>com.kitemc</groupId>
  <artifactId>kitemarket-api</artifactId>
  <version>1.0.0</version>
  <scope>provided</scope>
</dependency>
```

将认证写入用户的 `~/.m2/settings.xml`，`server.id` 必须和 `repository.id` 一致：

```xml
<server>
  <id>github-kitemarket</id>
  <username>${env.GITHUB_ACTOR}</username>
  <password>${env.GITHUB_TOKEN}</password>
</server>
```

## 第三方 GitHub Actions

Actions 使用 `GITHUB_TOKEN` 下载时需要 `packages: read`，并且该工作流仓库必须有访问对应包的权限。无法满足访问条件时，在第三方仓库配置名为 `PACKAGES_READ_TOKEN` 的 classic PAT secret，通过 `GITHUB_TOKEN: ${{ secrets.PACKAGES_READ_TOKEN }}` 传给上面的依赖配置。下载 SDK 不需要发包权限，也不需要 KiteMC 的 `RELEASES_TOKEN`。

## KiteMC 发行

公开仓库的 `Publish SDKs to GitHub Packages` 工作流在正式 Release 发布时自动运行，也可从 [Actions](https://github.com/KiteMC/KiteMarket/actions/workflows/packages.yml) 手动运行，输入已存在的公开标签，如 `v1.0.0`。工作流检出该标签，使用自带 `GITHUB_TOKEN` 的 `contents: read`／`packages: write`，无需另配跨仓库发包 PAT。

发布前检查两套 SDK 的 Java 11 字节码、公开类范围、POM 坐标和 MIT 许可。已有版本内容完全相同则跳过，内容不同则拒绝覆盖；上传中断留下部分版本时停止并提示管理员核对。新版本需要同步公开 SDK 源码和版本，再创建对应标签；不要用同一个版本名替换已有内容。

这条工作流只发布两套 SDK，不构建或上传运行插件、混淆映射、许可证配置或私有源码，也不创建正式 Release。原有混淆运行包发行流程不变。

出现 `401/403` 时检查 PAT 类型、`read:packages`、用户名、组织 SSO 授权及工作流访问权限；`404` 还可能表示对应版本尚无包。不要把令牌贴进日志。

参考 GitHub 官方说明：[Gradle Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry)、[Packages 权限](https://docs.github.com/en/packages/learn-github-packages/about-permissions-for-github-packages)、[Gradle Actions 发包](https://docs.github.com/en/actions/use-cases-and-examples/publishing-packages/publishing-java-packages-with-gradle)。
