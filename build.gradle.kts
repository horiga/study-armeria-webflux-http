import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

extra["kotlin.version"] = "1.4.0"

plugins {
    id("idea")
    id("java")
    id("org.springframework.boot") version "2.3.4.RELEASE"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"

    kotlin("jvm") version "1.4.0"
    kotlin("plugin.spring") version "1.4.0"
}

group = "org.horiga"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://repo.spring.io/milestone") }
}

dependencyManagement {
    imports {
        mavenBom("com.linecorp.armeria:armeria-bom:1.1.0")
        mavenBom("io.netty:netty-bom:4.1.52.Final")
        // mavenBom("org.testcontainers:testcontainers-bom:1.14.3")
    }
}

val ktlint: Configuration by configurations.creating

dependencies {

    // Armeria
    implementation("com.linecorp.armeria:armeria-spring-boot2-webflux-starter")
    implementation("com.linecorp.armeria:armeria-spring-boot2-actuator-starter")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation("io.micrometer:micrometer-registry-prometheus:1.5.2")

    // R2DBC, MySQL
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("com.github.jasync-sql:jasync-r2dbc-mysql:1.1.3")

    // Reactor
    implementation("io.projectreactor.addons:reactor-extra:3.3.3.RELEASE")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.0.2.RELEASE")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.testcontainers:testcontainers:1.14.3")
    testImplementation("org.testcontainers:junit-jupiter:1.14.3")

    ktlint("com.pinterest:ktlint:0.39.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.register<JavaExec>("ktlint") {
    group = "verification"
    description = "Kotlin code style check with ktlint."
    classpath = configurations.getByName("ktlint")
    main = "com.pinterest.ktlint.Main"
    args = listOf("--reporter=plain","--reporter=checkstyle,output=${buildDir}/reports/ktlint/ktlint-report.xml", "src/**/*.kt")
}
