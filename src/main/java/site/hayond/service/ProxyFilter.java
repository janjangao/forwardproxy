package site.hayond.service;

import io.micronaut.http.*;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.client.ProxyHttpClient;
import io.micronaut.http.client.exceptions.HttpClientException;
import io.micronaut.http.filter.*;
import jakarta.inject.Inject;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

@Filter("/**")
public class ProxyFilter implements HttpServerFilter {

    @Inject
    ForwardProxyService forwardProxyService;

    @Inject
    ProxyHttpClient proxyHttpClient;

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        boolean clear = request.getParameters().contains("clear");
        boolean debug = request.getParameters().contains("debug");
        ForwardTarget target = forwardProxyService.resolveTarget(request);
        Publisher<MutableHttpResponse<?>> publisherResponse;

        if (clear) {
            publisherResponse = Mono.just(HttpResponse.redirect(URI.create("/"))
                    .cookie(forwardProxyService.createHostForwardCookie(target, clear)));
        } else if (debug) {
            publisherResponse = Mono.from(forwardProxyService.buildDebugInfo(target))
                    .map(debugInfo -> HttpResponse.ok(debugInfo))
                    .map(response -> response.cookie(forwardProxyService.createHostForwardCookie(target, clear)));
        } else {
            publisherResponse = Mono
                    .from(proxyHttpClient
                            .proxy(request.mutate()
                                    .uri(b -> b.scheme(target.getScheme())
                                            .host(target.getHost())
                                            .port(target.getPort()))))
                    .doOnSuccess(
                            response -> response.cookie(forwardProxyService.createHostForwardCookie(target, clear)))
                    .onErrorResume(e -> {
                        if (e instanceof HttpClientException) {
                            return Mono.just(HttpResponse.serverError()
                                    .body(Map.of("error", e.getMessage())));
                        }
                        return Mono.error(e);
                    });
        }

        return publisherResponse;
    }

}
