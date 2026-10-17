package com.uniminuto.clinica;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "spring.datasource.password=test-password")
class ClinicaApplicationTests {

	@LocalServerPort
	private int port;

	@Test
	void contextLoads() {
	}

	@Test
	void healthCheckEndpointAcceptsGetWithoutContentType() throws Exception {
		var request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + port + "/clinica/v1/clinica/test"))
				.header("Origin", "http://localhost:4200")
				.GET()
				.build();

		var response = HttpClient.newHttpClient().send(
				request,
				HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());
		assertEquals("Servicio funcionando correctamente", response.body());
		assertEquals(
				"*",
				response.headers().firstValue("Access-Control-Allow-Origin").orElse(null));
	}

	@Test
	void corsPreflightAllowsFrontendGetRequest() throws Exception {
		var request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + port + "/clinica/v1/clinica/test"))
				.header("Origin", "http://localhost:4200")
				.header("Access-Control-Request-Method", "GET")
				.header("Access-Control-Request-Headers", "content-type,authorization")
				.method("OPTIONS", HttpRequest.BodyPublishers.noBody())
				.build();

		var response = HttpClient.newHttpClient().send(
				request,
				HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());
		assertEquals(
				"*",
				response.headers().firstValue("Access-Control-Allow-Origin").orElse(null));
	}

}
