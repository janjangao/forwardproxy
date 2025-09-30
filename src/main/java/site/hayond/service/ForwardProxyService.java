package site.hayond.service;

import io.micronaut.http.*;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.cookie.Cookie;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.reactivestreams.Publisher;

@Singleton
public class ForwardProxyService {

    private final HttpClient httpClient;
    private final ForwardProxyConfig config;

    public ForwardProxyService(HttpClient httpClient, ForwardProxyConfig config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    public ForwardTarget resolveTarget(HttpRequest<?> request) {
        String portQuery = request.getParameters().get(config.getPortQuery());
        String hostQuery = request.getParameters().get(config.getHostQuery());
        String query = hostQuery != null ? hostQuery : portQuery;
        String cookie = request.getCookies().findCookie(config.getCookie())
                .map(Cookie::getValue).orElse(null);
        String defaultHost = config.getDefaultHost();
        int defaultPort = config.getDefaultPort();

        if (query != null && !query.isEmpty()) {
            string finalHost = hostQuery != null && !hostQuery.isEmpty() ? hostQuery : defaultHost;
            string finalPort = portQuery != null && !portQuery.isEmpty() ? portQuery : String.valueOf(defaultPort);
            return new ForwardTarget(finalHost, finalPort);
        } else if (cookie != null && !cookie.isEmpty()) {
            return new ForwardTarget(cookie, defaultPort);
        }
        return new ForwardTarget(defaultHost, defaultPort);
    }

    public Cookie createHostForwardCookie(ForwardTarget target, boolean clear) {
        String value = clear ? "" : target.getUrl();
        Cookie cookie = Cookie.of(config.getCookie(), value);
        cookie.path("/");
        cookie.httpOnly(true);
        cookie.maxAge(Duration.ofDays(30));
        if (clear)
            cookie.maxAge(0);
        return cookie;
    }

    public Publisher<Map<String, Object>> buildDebugInfo(ForwardTarget target) {
        return Mono.from(buildTargetInfo(target))
                .map(targetInfo -> {
                    Map<String, Object> debugInfo = new HashMap<>();
                    debugInfo.put("target", targetInfo);
                    debugInfo.put("config", config);
                    return debugInfo;
                });
    }

    public Publisher<Map<String, Object>> buildTargetInfo(ForwardTarget target) {
        Map<String, Object> result = new HashMap<>();
        result.put("host", target.getUrl());
        HttpRequest<?> probeRequest = HttpRequest.HEAD(target.getUri());
        return Mono.from(httpClient.exchange(probeRequest))
                .map(response -> {
                    int statusCode = response.getStatus().getCode();
                    result.put("reachable", statusCode < 500);
                    result.put("statusCode", statusCode);
                    return result;
                })
                .onErrorResume(e -> {
                    result.put("reachable", false);
                    result.put("error", e.getMessage());
                    return Mono.just(result);
                });
    }
}
