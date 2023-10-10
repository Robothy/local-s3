package com.robothy.s3.rest.handler;

import com.robothy.netty.http.HttpRequest;
import com.robothy.netty.http.HttpRequestHandler;
import com.robothy.netty.router.AbstractRouter;
import com.robothy.netty.router.Route;
import com.robothy.netty.router.Router;
import io.netty.handler.codec.http.HttpMethod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

class LocalS3Router extends AbstractRouter {

  static final String BUCKET_PATH = "/{bucket}";

  static final String BUCKET_KEY_PATH = "/{bucket}/{key}";

  private final Map<HttpMethod, Map<String, List<Route>>> rules = new HashMap<>();

  LocalS3Router() {

  }

  @Override
  public Router route(Route rule) {
    this.rules.putIfAbsent(rule.getMethod(), new HashMap<>());
    Map<String, List<Route>> pathRules = this.rules.get(rule.getMethod());
    pathRules.putIfAbsent(rule.getPath(), new ArrayList<>());
    List<Route> routes = pathRules.get(rule.getPath());
    routes.add(rule);
    return this;
  }

  @Override
  public HttpRequestHandler match(HttpRequest request) {
    return matchMethod(request.getMethod())
        .map(pathRules -> matchPath(pathRules, request))
        .map(rules -> matchHandler(rules, request))
        .orElse(notFoundHandler());
  }

  Optional<Map<String, List<Route>>> matchMethod(HttpMethod method) {
    return Optional.ofNullable(this.rules.get(method));
  }

  List<Route> matchPath(Map<String, List<Route>> pathRules, HttpRequest request) {
    String path = request.getPath();
    String trimmedPath = trimPath(path);
    if (pathRules.containsKey(trimmedPath)) {
      return pathRules.get(trimmedPath);
    }

    Map<CharSequence, List<String>> params = request.getParams();
    int slashCount = StringUtils.countMatches(path, '/');
    if (slashCount == 1 || (slashCount == 2 && path.endsWith("/"))) {
      return getHandlerForBucketOperation(pathRules, trimmedPath, params);
    } else {
      return getHandlerForObjectOperation(pathRules, path, params);
    }
  }

  String trimPath(String path) {
    if ("/".equals(path)) {
      return path;
    }

    path = path.trim();
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    return path;
  }

  List<Route> getHandlerForBucketOperation(Map<String, List<Route>> pathRules,
                                           String trimmedPath, Map<CharSequence, List<String>> params) {
    params.put("bucket", List.of(trimmedPath.substring(1)));
    return pathRules.get(BUCKET_PATH);
  }

  List<Route> getHandlerForObjectOperation(Map<String, List<Route>> pathRules,
                                           String path, Map<CharSequence, List<String>> params) {
    int secondSlashIdx = path.indexOf('/', 1);
    params.put("bucket", List.of(path.substring(1, secondSlashIdx)));
    params.put("key", List.of(path.substring(secondSlashIdx + 1)));
    return pathRules.get(BUCKET_KEY_PATH);
  }

  HttpRequestHandler matchHandler(List<Route> candidates, HttpRequest request) {
    HttpRequestHandler result = null;
    int priority = 0;
    for (Route candidate : candidates) {
      int currentPriority = calculatePriority(candidate, request);
      if (currentPriority >= priority) {
        priority = currentPriority;
        result = candidate.getHandler();
      }
    }
    return result;
  }

  int calculatePriority(Route route, HttpRequest request) {

    int priority = 0;

    if (Objects.isNull(route.getHeaderMatcher())) {
      priority |= (1<<1);
    } else if (route.getHeaderMatcher().apply(request.getHeaders())) {
      priority |= (1 << 3);
    } else {
      priority = -1;
    }

    if (Objects.isNull(route.getParamMatcher())) {
      priority |= (1<<2);
    } else if (route.getParamMatcher().apply(request.getParams())) {
      priority |= (1 << 4);
    } else {
      priority = -1;
    }

    return priority;
  }

}
