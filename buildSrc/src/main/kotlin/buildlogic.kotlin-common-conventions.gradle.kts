import kotlinSerializationVersion
import kotlinVersion
import ktorVersion
import testCoroutinesVersion

plugins {

    id("buildlogic.kotlin-common-conventions")
    'java-library'
}

repositories {
    mavenCentral()
    maven {
        name = "PublicGitHubPackages"
        url = uri("https://maven.pkg.github.com/pavelo8501/ReKotlin")
    }
}

dependencies {

    constraints {
        implementation("io.ktor:ktor-server-core:$ktorVersion")
        implementation("io.ktor:ktor-server-netty:$ktorVersion")
        implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")
    }


    testImplementation('libs.junit.jupiter')
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$testCoroutinesVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform() // <5>
}
