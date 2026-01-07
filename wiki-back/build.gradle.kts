plugins {
	java
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "wiki"
version = "0.0.1-SNAPSHOT"
description = "Wikipedia Real-time Playground"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Spring Web
	implementation("org.springframework.boot:spring-boot-starter-web")

	// Kafka
	implementation("org.springframework.kafka:spring-kafka")

	// Spring Kafka Test
	testImplementation("org.springframework.kafka:spring-kafka-test")

	// Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// Spring Boot Test
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	// JUnit
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// Redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	// Jackson JSR310
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}

tasks.withType<Test> {
	useJUnitPlatform()
}
