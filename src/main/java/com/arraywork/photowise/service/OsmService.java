package com.arraywork.photowise.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.arraywork.photowise.entity.OsmLocation;
import com.arraywork.springforce.error.HttpException;
import com.arraywork.springforce.util.Jackson;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Open Street Map APIs
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/21
 */
@Service
public class OsmService {

    private static final String BASE_URL = "https://nominatim.openstreetmap.org";
    private final RestClient restClient;

    @Resource
    private Jackson jackson;

    /** Initialize rest client */
    @Autowired
    public OsmService(@Value("${photowise.title}") String title, @Value("${photowise.version}") String version) {
        // 国内无法访问 nominatim API，因此需要代理
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8889));
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setProxy(proxy);

        restClient = RestClient.builder()
            .requestFactory(requestFactory)
            .baseUrl(BASE_URL)
            .defaultHeader("User-Agent", title + "@" + version)
            .build();
    }

    /** Reverse geographic coordinates */
    public OsmLocation reverse(double lat, double lon) {
        JsonNode result = restClient.get()
            .uri("/reverse?format=json&lat={lat}&lon={lon}", lat, lon)
            .retrieve()
            .onStatus(HttpStatusCode::isError, new RestErrorHandler())
            .body(JsonNode.class);

        JsonNode osmId = result.get("osm_id");
        if (osmId != null) {
            JsonNode address = result.get("address");
            OsmLocation osmLocation = jackson.convertToEntity(address, OsmLocation.class);
            osmLocation.setOsmId(osmId.asText());
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