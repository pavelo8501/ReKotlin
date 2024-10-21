
val kotlinVersion: String by project
val ktorVersion: String by project
val exposedVersion: String by project
val kotlinSerializationVersion: String by project
val hikaricpVersion: String by project
val mysqlVersion: String by project


plugins {
    id("buildlogic.kotlin-application-conventions")
}





group = "po.playground"
version = "1.0.0"

repositories {
    mavenCentral()
    maven {
        name = "PublicGitHubPackages"
        url = uri("https://maven.pkg.github.com/pavelo8501/ReKotlin")
    }
}

dependencies {

    implementation(project(":ExposedDAOWrapper"))
    implementation(project(":RestApiServerWrapper"))
    implementation(project(":WsApiServerWrapper"))


   // implementation("io.ktor:ktor-server-core:$ktorVersion")
   // implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")

    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")

    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")

    implementation("io.ktor:ktor-server-cors:$ktorVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
    implementation("com.zaxxer:HikariCP:$hikaricpVersion")
    implementation("mysql:mysql-connector-java:$mysqlVersion")

    implementation("io.github.cdimascio:dotenv-kotlin:6.3.1")

    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.12")
}

application {
    mainClass = ("po.playground.MainKt")
}


kotlin {
    jvmToolchain(22)
}

tasks.register<Exec>("dockerComposeUp") {
    commandLine("docker-compose", "up", "-d")
}

tasks.named("run") {
    dependsOn("dockerComposeUp")
}