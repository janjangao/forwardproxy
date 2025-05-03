package site.hayond.service;

import io.micronaut.http.*;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.client.ProxyHttpClient;
import io.micronaut.http.filter.*;
import jakarta.inject.Inject;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Map;

@Filter("/**")
public class ProxyFilter implements HttpServerFilter {

    @Inject
    ForwardProxyService forwardProxyService;

    @Inject
    ProxyHttpClient proxyHttpClient;

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        boolean debug = request.getParameters().contains("debug");
        boolean clear = request.getParameters().contains("clear");
        ForwardTarget target = forwardProxyService.resolveTarget(request, clear);
        Publisher<MutableHttpResponse<?>> publisherResponse;

        if (debug) {
            publisherResponse = Mono.from(forwardProxyService.buildDebugInfo(request, target))
                    .map(debugInfo -> HttpResponse.ok(debugInfo))
                    .map(response -> addForwardCookie(response, target, clear));
        } else {
            publisherResponse = Mono
                    .from(proxyHttpClient
                            .proxy(request.mutate()
                                    .uri(b -> b.scheme(target.getScheme())
                                            .host(target.getHost())
                                            .port(target.getPort()))))
                    .doOnSuccess(response -> addForwardCookie(response, target, clear))
                    .onErrorResume(e -> Mono.just(HttpResponse.serverError()
                            .body(Map.of("error", e.getMessage()))));
        }

        return publisherResponse;
    }

    private MutableHttpResponse<?> addForwardCookie(MutableHttpResponse<?> response, ForwardTarget target,
            boolean clear) {
        response.cookie(forwardProxyService.createHostForwardCookie(target, clear));
        return response;
    }
}
