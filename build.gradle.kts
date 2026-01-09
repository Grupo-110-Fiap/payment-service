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
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.test {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

sonarqube {
	properties {
		property("sonar.projectKey", "Grupo-110-Fiap_payment-service") // Ajuste conforme gerado no Sonar
		property("sonar.organization", "grupo-110-fiap")
		property("sonar.host.url", "https://sonarcloud.io")

		property("sonar.exclusions", "**/config/**, **/model/**, **/dto/**")
	}
}

tasks.jacocoTestReport {
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
	dependsOn(tasks.test)
}
