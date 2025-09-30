# Forward Proxy

[forwardproxy](https://github.com/janjangao/forwardproxy) Forward port with tailscale domain by query. `hostname.xxx.ts.net?port=9000`

## Step

### Run forwardproxy with docker
DockerHub: [janjangao/forwardproxy](https://hub.docker.com/r/janjangao/forwardproxy)

```
docker run --network=host --restart unless-stopped -d --name forwardproxy janjangao/forwardproxy
```
docker compose
```
services:
  forwardproxy:
    image: janjangao/forwardproxy
    container_name: forwardproxy
    network_mode: host
    restart: unless-stopped
```

### Run without host network mode.
By right, container can not access host `localhost` network, so we need run with `--network=host`, but container has a bidge IP `172.17.0.1` can access host, so can assign it as default host by env variable.
```
# 8888 is server port
docker run -p 8080:8080 -e PORT_FORWARD_DEFAULT_HOST=172.17.0.1 --restart unless-stopped -d --name forwardproxy janjangao/forwardproxy
```

### Tailscale Funnel on forwardproxy
```
tailscale funnel --bg 8080
```

### Test locally
For example, you have a portainer deployed on localhost:9000, try access `http://localhost:8080?port=9000`, you can access portainer as well, the query shall be saved on cookie after first access, next time you only need vist `http://localhost:8080` with same browser, if need change forward port, put the query `port=5244` in url again.

### Access Tailscale domain
`https://{yourdomain}?port=9000`

### Tailscale in docker
It has same host network accessbility problem(container can't access host `localhost`), tailscale needs run with `--network=host` as well.

## Query

### ?port=9000

### ?host=172.17.0.1:9000
Port forward shall be enough for tailscale funnel, if you want to forward host you can try this

### ?clear
Clear the forward cookie

### ?debug
debug info 

## ENV

### MICRONAUT_SERVER_PORT
Server port, value: 8080

### PORT_FORWARD_DEFAULT_PORT
Default forward port if don't have any query, value: 80

### PORT_FORWARD_DEFAULT_HOST
Default forward host if don't any query, value: localhost
