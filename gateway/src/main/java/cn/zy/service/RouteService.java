package cn.zy.service;

import com.google.common.collect.Lists;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RouteService implements ApplicationRunner, ApplicationEventPublisherAware {

  private ApplicationEventPublisher publisher;

  @Autowired
  private RouteDefinitionWriter routeDefinitionWriter;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    RouteDefinition definition = new RouteDefinition();
    List<FilterDefinition> filterList = new ArrayList<>();
    definition.setFilters(filterList);
    definition.setId("order-service");
    definition.setUri(URI.create("lb://order-service"));
    definition.setOrder(-2);
    this.buildRoutePredict("/finderchina/finder-proxy/price-tag/vehicle", definition);
    this.buildPrefix(5, "/finderchina/api/v2/listing", definition);
//    this.buildHystrix(customRoute, definition);
    routeDefinitionWriter.save(Mono.just(definition)).subscribe();
    this.publisher.publishEvent(new RefreshRoutesEvent(this));
  }

  private void buildPrefix(int count, String prefixPath, RouteDefinition routeDefinition) {
    if (count > 0) {
      FilterDefinition countFilter = new FilterDefinition();
      countFilter.setName("CustomStripPrefix");
      countFilter.addArg("parts", Integer.toString(count));
      routeDefinition.getFilters().add(countFilter);
    }
    if (StringUtils.isNotEmpty(prefixPath)) {
      FilterDefinition prefixPathFilter = new FilterDefinition();
      prefixPathFilter.setName("PrefixPath");
      prefixPathFilter.addArg("prefix", prefixPath);
      routeDefinition.getFilters().add(prefixPathFilter);
    }
  }

  private void buildRoutePredict(String matchPath, RouteDefinition definition) {
    PredicateDefinition predicate = new PredicateDefinition();
    predicate.setName("Path");
    predicate.addArg("pattern", matchPath);
    definition.setPredicates(Lists.newArrayList(predicate));
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.publisher = applicationEventPublisher;
  }
}
