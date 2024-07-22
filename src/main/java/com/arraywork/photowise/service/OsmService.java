package com.arraywork.photowise.service;

import java.io.IOException;
import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.arraywork.photowise.entity.OsmAddress;
import com.arraywork.springforce.error.HttpException;
import com.arraywork.springforce.util.Jackson;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Open Street Map APIs
 * The original API 'https://nominatim.openstreetmap.org' has been blocked in mainland China.
 * Maybe you need a proxy like this:
 * <pre>
 *   Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8889));
 *   SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
 *   requestFactory.setProxy(proxy);
 *   RestClient.builder().requestFactory(requestFactory)...
 * </pre>
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/21
 */
@Service
public class OsmService {

    private static final String BASE_URL = "https://api.unpkg.net/geo";
    private final RestClient restClient;

    @Resource
    private Jackson jackson;

    /** Initialize rest client */
    @Autowired
    public OsmService(@Value("${photowise.title}") String title, @Value("${photowise.version}") String version) {
        restClient = RestClient.builder()
            .baseUrl(BASE_URL)
            .defaultHeader("User-Agent", title + "@" + version)
            .build();
    }

    /** Reverse geographic coordinates */
    public OsmAddress reverse(double lat, double lon) {
        JsonNode result = restClient.get()
            .uri("/reverse?format=json&lat={lat}&lon={lon}", lat, lon)
            .retrieve()
            .onStatus(HttpStatusCode::isError, new RestErrorHandler())
            .body(JsonNode.class);

        JsonNode osmId = result.get("osm_id");
        if (osmId != null) {
            JsonNode address = result.get("address");
            OsmAddress osmAddress = jackson.convertToEntity(address, OsmAddress.class);
            osmAddress.setOsmId(osmId.asText());
        }
        return null;
    }

    /** Rest error handler */
    class RestErrorHandler implements RestClient.ResponseSpec.ErrorHandler {
        @Override
        public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {
            String body = new String(response.getBody().readAllBytes());
            JsonNode error = jackson.parse(body, JsonNode.class).path("error");
            String message = error.path("message").asText("Request '" + BASE_URL + "' failed.");
            throw new HttpException(response.getStatusCode(), message);
        }
    }

}