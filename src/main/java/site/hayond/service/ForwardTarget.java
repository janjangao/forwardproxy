package site.hayond.service;

import java.net.URI;

public class ForwardTarget {

    public static final String SCHEME_HTTP = "http";
    public static final String SCHEME_HTTPS = "https";
    public static final String LOCAL_HOST = "localhost";
    public static final int PORT_HTTP = 80;
    public static final int PORT_HTTPS = 443;
    public static final String DEFAULT_HOST = SCHEME_HTTP + "://" + LOCAL_HOST;

    private final Location location;

    public ForwardTarget(String target, int port) {
        this.location = new Location(normalizeTarget(target, port));
    }

    public ForwardTarget(String target, String port) {
        this(target, port.matches("\\d+") ? Integer.parseInt(port) : PORT_HTTP);
    }

    public ForwardTarget(String target) {
        this(target, PORT_HTTP);
    }

    public Location getLocation() {
        return location;
    }

    public URI getUri() {
        return location.getUri();
    }

    public String getScheme() {
        return location.getProtocol();
    }

    public String getHost() {
        return location.getHost();
    }

    public int getPort() {
        return location.getPort();
    }

    public String getUrl() {
        return location.getHref();
    }

    private URI normalizeTarget(String target, int port) {
        String url;

        if (target == null || target.isEmpty()) {
            url = port == PORT_HTTP ? DEFAULT_HOST : DEFAULT_HOST + ":" + port;
        } else if (target.matches("\\d+")) {
            url = LOCAL_HOST + ":" + target;
        } else if (target.matches(".*:\\d+$")) {
            url = target;
        } else {
            url = target + ":" + port;
        }

        if (!url.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
            url = SCHEME_HTTP + "://" + url;
        }

        try {
            URI uri = new URI(url);

            String scheme = uri.getScheme() != null ? uri.getScheme().toLowerCase() : SCHEME_HTTP;
            String host = uri.getHost() != null ? uri.getHost().toLowerCase() : DEFAULT_HOST;
            int uriPort = uri.getPort();

            if ((scheme.equals(SCHEME_HTTP) && uriPort == PORT_HTTP)
                    || (scheme.equals(SCHEME_HTTPS) && uriPort == PORT_HTTPS)) {
                uriPort = -1;
            }
            if ((scheme.equals(SCHEME_HTTP) && uriPort == PORT_HTTPS)) {
                scheme = SCHEME_HTTPS;
                uriPort = -1;
            }

            return new URI(scheme, null, host, uriPort, null, null, null);
        } catch (Exception e) {
            String fallbackUrl = DEFAULT_HOST + (port == PORT_HTTP ? "" : ":" + port);
            return URI.create(fallbackUrl);
        }
    }

}
