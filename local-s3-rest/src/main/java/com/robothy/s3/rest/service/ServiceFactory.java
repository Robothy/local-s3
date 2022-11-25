package com.robothy.s3.rest.service;

import java.util.function.Supplier;

public interface ServiceFactory {

  <T> void register(Class<T> clazz, Supplier<? extends T> factory);

  <T> T getInstance(Class<T> clazz);

}
