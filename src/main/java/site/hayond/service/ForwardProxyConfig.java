package site.hayond.service;

import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.context.annotation.ConfigurationProperties;

@Serdeable
@ConfigurationProperties("port.forward")
public class ForwardProxyConfig {
    private String defaultHost = "localhost";
    private int defaultPort = 80;
    private String portQuery = "port";
    private String hostQuery = "host";
    private String cookie = "forward-proxy";

    public String getDefaultHost() {
        return defaultHost;
    }

    public void setDefaultHost(String defaultHost) {
        this.defaultHost = defaultHost;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public void setDefaultPort(int defaultPort) {
        this.defaultPort = defaultPort;
    }

    public String getPortQuery() {
        return portQuery;
    }

    public void setPortQuery(String portQuery) {
        this.portQuery = portQuery;
    }

    public String getHostQuery() {
        return hostQuery;
    }

    public void setHostQuery(String hostQuery) {
        this.hostQuery = hostQuery;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

}
