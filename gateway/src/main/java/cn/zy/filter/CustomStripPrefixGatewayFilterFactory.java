package cn.zy.filter;

import static org.springframework.cloud.gateway.support.GatewayToStringStyler.filterToStringCreator;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

import java.util.Arrays;
import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomStripPrefixGatewayFilterFactory extends
    AbstractGatewayFilterFactory<CustomStripPrefixGatewayFilterFactory.Config> {

  /**
   * Parts key.
   */
  public static final String PARTS_KEY = "parts";

  public CustomStripPrefixGatewayFilterFactory() {
    super(Config.class);
  }

  @Override
  public List<String> shortcutFieldOrder() {
    return Arrays.asList(PARTS_KEY);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return new GatewayFilter() {
      @Override
      public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        addOriginalRequestUrl(exchange, request.getURI());
        String path = request.getURI().getRawPath();
        String[] originalParts = StringUtils.tokenizeToStringArray(path, "/");

        // all new paths start with /
        StringBuilder newPath = new StringBuilder("/");
        for (int i = 0; i < originalParts.length; i++) {
          if (i >= config.getParts()) {
            // only append slash if this is the second part or greater
            if (newPath.length() > 1) {
              newPath.append('/');
            }
            newPath.append(originalParts[i]);
          }
        }
        if (newPath.length() > 1 && path.endsWith("/")) {
          newPath.append('/');
        }

        ServerHttpRequest newRequest = request.mutate().path(newPath.toString()).build();

        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, newRequest.getURI());

        return chain.filter(exchange.mutate().request(newRequest).build());
      }

      @Override
      public String toString() {
        return filterToStringCreator(CustomStripPrefixGatewayFilterFactory.this).append("parts",
                config.getParts())
            .toString();
      }
    };
  }

  public static class Config {

    private int parts = 1;

    public int getParts() {
      return parts;
    }

    public void setParts(int parts) {
      this.parts = parts;
    }

  }
}
