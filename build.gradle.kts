import java.math.BigDecimal

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"

	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.7"
	id("jacoco")
	id("org.sonarqube") version "5.0.0.4638"
}

group = "br.com.fiap.techchallenge"
version = "0.0.1"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

val kotlinLoggingJvmVersion = "7.0.13"
val awsParameterStoreVersion = "3.1.0"
val mockkVersion = "1.13.8"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("io.awspring.cloud:spring-cloud-aws-starter-sqs:${awsParameterStoreVersion}")
	implementation("io.awspring.cloud:spring-cloud-aws-starter-sns:${awsParameterStoreVersion}")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("io.github.oshai:kotlin-logging-jvm:$kotlinLoggingJvmVersion")
	implementation("io.awspring.cloud:spring-cloud-aws-starter-parameter-store:$awsParameterStoreVersion")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.mockk:mockk:$mockkVersion")
	testImplementation("org.junit.platform:junit-platform-launcher")

	// Cucumber BDD
	testImplementation("io.cucumber:cucumber-java:7.15.0")
	testImplementation("io.cucumber:cucumber-spring:7.15.0")
	testImplementation("io.cucumber:cucumber-junit-platform-engine:7.15.0")
	testImplementation("org.junit.platform:junit-platform-suite:1.10.0")
}

kotlin {

	jvmToolchain(21)

	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.test {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		csv.required.set(false)
		html.required.set(true)
	}

	classDirectories.setFrom(files(classDirectories.files.map {
		fileTree(it) {
			exclude(
				"**/dto/**",
				"**/domain/**",
				"**/configuration/**",
				"**/*Application*"
			)
		}
	}))
}

tasks.jacocoTestCoverageVerification {
	dependsOn(tasks.test)
	violationRules {
		rule {
			limit {
				minimum = BigDecimal("0.80")
			}
		}
	}
}
