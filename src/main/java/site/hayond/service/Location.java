package site.hayond.service;

import java.net.URI;

public class Location {
    private URI uri;

    public Location(URI uri) {
        this.uri = uri;
    }

    public Location(String uri) {
        this.uri = URI.create(uri);
    }

    public URI getUri() {
        return uri;
    }

    public String getHost() {
        return uri.getHost();
    }

    public int getPort() {
        return uri.getPort();
    }

    public String getProtocol() {
        return uri.getScheme();
    }

    public String getHostname() {
        return uri.getHost();
    }

    public String getHref() {
        return uri.toASCIIString();
    }

    public String getOrigin() {
        return uri.getScheme() + "://" + uri.getHost();
    }

    public String getPathname() {
        return uri.getPath();
    }

    public String getSearch() {
        return uri.getQuery();
    }

    public String getHash() {
        return uri.getFragment();
    }

}
