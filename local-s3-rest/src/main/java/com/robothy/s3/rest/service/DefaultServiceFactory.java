package com.robothy.s3.rest.service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultServiceFactory implements ServiceFactory {

  private static final Map<Class<?>, Supplier<?>> factoryMap = new HashMap<>();

  @Override
  public <T> void register(Class<T> clazz, Supplier<? extends T> factory) {
    factoryMap.put(clazz, factory);
    log.debug("Registered service " + factory.get().getClass().getName() + ".");
  }

  @Override
  public <T> T getInstance(Class<T> clazz) {
    if (!factoryMap.containsKey(clazz)) {
      throw new IllegalArgumentException("Not cannot find service factory for " + clazz.getName() + ".");
    }

    //noinspection unchecked
    return (T) factoryMap.get(clazz).get();
  }

}
