

plugins {
    `java-library`
    `maven-publish`
    id("io.spring.dependency-management") version "1.1.6"
    id("com.diffplug.spotless") version "6.11.0"
}
group = "com.github.petebids"
version = "0.0.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

dependencies {
    // https://mvnrepository.com/artifact/dev.cerbos/cerbos-sdk-java
    api("dev.cerbos:cerbos-sdk-java:0.12.0")
    // https://mvnrepository.com/artifact/com.google.protobuf/protobuf-java
    api("com.google.protobuf:protobuf-java:4.27.0")
    // https://mvnrepository.com/artifact/com.google.protobuf/protobuf-java-util
    api("com.google.protobuf:protobuf-java-util:4.27.0")
    api("org.springframework.boot:spring-boot-starter-data-jpa:3.3.0")
    testRuntimeOnly("org.postgresql:postgresql:42.6.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    implementation(platform("org.testcontainers:testcontainers-bom:1.17.6"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.0")
    testImplementation("org.springframework.boot:spring-boot-testcontainers:3.3.0")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("com.github.gavlyukovskiy:datasource-proxy-spring-boot-starter:1.10.0")


    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

//tasks.withType<JavaCompile>{
//    dependsOn(tasks.withType<SpotlessApply>())
//}

spotless{
    java{
        googleJavaFormat()
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            group = groupId
            artifactId = "cerbos-jpa-query-plan-adapter"
            version = version
        }

    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/petebids/cerbos-jpa-query-plan-adapter")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}


