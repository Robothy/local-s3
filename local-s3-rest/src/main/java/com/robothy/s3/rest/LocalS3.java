package com.robothy.s3.rest;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.robothy.netty.initializer.HttpServerInitializer;
import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.core.service.ObjectService;
import com.robothy.s3.core.service.manager.LocalS3Manager;
import com.robothy.s3.rest.bootstrap.LocalS3Mode;
import com.robothy.s3.rest.handler.LocalS3RouterFactory;
import com.robothy.s3.rest.service.DefaultServiceFactory;
import com.robothy.s3.rest.service.ServiceFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.stream.XMLInputFactory;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * LocalS3 service launcher.
 */
@Slf4j
public class LocalS3 {

  /* Configurations */
  @Getter
  private int port = 8080;

  @Getter
  private Path dataPath;

  private LocalS3Mode mode = LocalS3Mode.IN_MEMORY;

  private boolean initialDataCacheEnabled = true;

  private int nettyParentEventGroupThreadNum = 1;

  private int nettyChildEventGroupThreadNum = 2;

  private int s3ExecutorThreadNum = 4;


  /* Private fields. */
  private NioEventLoopGroup parentGroup;

  private NioEventLoopGroup childGroup;

  private EventExecutorGroup executorGroup;

  private Channel serverSocketChannel;

  /**
   * Create a {@linkplain Builder}.
   *
   * @return a new {@linkplain Builder} instance.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Startup the local-s3 service.
   */
  @SneakyThrows
  public void start() {
    ServiceFactory serviceFactory = createServiceFactory();

    this.parentGroup = new NioEventLoopGroup(nettyParentEventGroupThreadNum);
    this.childGroup = new NioEventLoopGroup(nettyChildEventGroupThreadNum);
    this.executorGroup = new DefaultEventLoopGroup(s3ExecutorThreadNum);
    ServerBootstrap serverBootstrap = new ServerBootstrap();
    ChannelFuture channelFuture = serverBootstrap.group(parentGroup, childGroup)
        .handler(new LoggingHandler(LogLevel.DEBUG))
        .channel(NioServerSocketChannel.class)
        .childHandler(new HttpServerInitializer(executorGroup, LocalS3RouterFactory.create(serviceFactory)))
        .bind(port)
        .sync();
    log.info("LocalS3 started.");
    this.serverSocketChannel = channelFuture.channel();
    Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
  }

  private ServiceFactory createServiceFactory() {

    LocalS3Manager manager;
    if (mode == LocalS3Mode.IN_MEMORY) {
      log.info("Created in-memory LocalS3 manager.");
      manager = LocalS3Manager.createInMemoryS3Manager(dataPath, initialDataCacheEnabled);
    } else {
      log.info("Created file system LocalS3 manager.");
      manager = LocalS3Manager.createFileSystemS3Manager(dataPath);
    }

    ServiceFactory serviceFactory = new DefaultServiceFactory();
    BucketService bucketService = manager.bucketService();
    ObjectService objectService = manager.objectService();
    serviceFactory.register(BucketService.class, () -> bucketService);
    serviceFactory.register(ObjectService.class, () -> objectService);

    XMLInputFactory input = new WstxInputFactory();
    input.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
    XmlMapper xmlMapper = new XmlMapper(new XmlFactory(input, new WstxOutputFactory()));
    xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    xmlMapper.registerModule(new Jdk8Module());
    xmlMapper.registerModule(new JavaTimeModule());
    serviceFactory.register(XmlMapper.class, () -> xmlMapper);
    return serviceFactory;
  }

  /**
   * Shutdown the local-s3 service.
   */
  public void shutdown() {
    if (null == this.parentGroup || null == this.childGroup) {
      throw new IllegalStateException("LocalS3 is not started.");
    }

    try {
      if (this.serverSocketChannel.isOpen()) {
        this.serverSocketChannel.close().sync();
      }
      log.info("LocalS3 stopped.");
    } catch (InterruptedException e) {
      log.error("Close server socket channel failed.", e);
    } finally {
      shutdownEventExecutorsGroupIfNeeded(this.executorGroup, this.childGroup, this.parentGroup);
    }
  }

  private void shutdownEventExecutorsGroupIfNeeded(EventExecutorGroup... eventExecutorsList) {
    for (EventExecutorGroup eventExecutors : eventExecutorsList) {
      if (!eventExecutors.isShuttingDown() && !eventExecutors.isShutdown()) {
        eventExecutors.shutdownGracefully();
      }
    }
  }

  public static class Builder {

    private final LocalS3 propHolder = new LocalS3();

    /**
     * Set the port that local-s3 service listen to. Default port is 8080.
     * Set the value to {@code -1} if you want to assign a random port.
     *
     * @param port customized port.
     * @return builder.
     */
    public Builder port(int port) {
      if (port < 0) {
        propHolder.port = findFreeTcpPort();
      } else {
        propHolder.port = port;
      }
      return this;
    }

    /**
     * Set the LocalS3 data directory. The default value is {@code null},
     * while data is stored in Java Heap.
     *
     * <p>
     * If the path is specified and the {@code mode} is {@code PERSISTENCE},
     * then the LocalS3 service load data from and store data in this directory.
     *
     * <p>
     * If the data directory is set and LocalS3 runs in {@code IN_MEMORY} mode,
     * then data from that path will be loaded as initial data. All changes are
     * only available in the memory, i.e. won't write back to the specified path.
     * <p>
     * Besides, LocalS3 will cache accessed data from this path; which could reduce
     * disk I/O when start LocalS3 in {@code IN_MEMORY} mode with the same initial
     * data for multi-times.
     *
     * @param dataPath data path.
     * @return builder.
     */
    public Builder dataPath(String dataPath) {
      this.propHolder.dataPath = dataPath == null ? null : Paths.get(dataPath);
      return this;
    }

    /**
     * Set LocalS3 service running mode. Default value is {@code PERSISTENCE}.
     *
     * @param mode LocalS3 service running mode.
     * @return builder.
     */
    public Builder mode(LocalS3Mode mode) {
      propHolder.mode = mode;
      return this;
    }

    /**
     * This option only available when running LocalS3 in {@code IN_MEMORY} mode
     * with initial data. If initial data cache is enabled, LocalS3 caches the
     * accessed initial data in memory. This could reduce dist I/O when running
     * tests with initial data in the same path.
     *
     * <p> The default value is {@code true}.
     *
     * @param enabled is the initial data cache enabled.
     * @return if the initial data cache enabled.
     */
    public Builder initialDataCacheEnabled(boolean enabled) {
      this.propHolder.initialDataCacheEnabled = enabled;
      return this;
    }

    /**
     * Set netty parent event group thread number.
     * Default values is 1.
     *
     * @param nettyParentEventGroupThreadNum netty parent event group thread number.
     * @return builder.
     */
    public Builder nettyParentEventGroupThreadNum(int nettyParentEventGroupThreadNum) {
      propHolder.nettyParentEventGroupThreadNum = nettyParentEventGroupThreadNum;
      return this;
    }

    /**
     * Set netty child event group thread number.
     * Default value is 2.
     *
     * @param nettyChildEventGroupThreadNum netty child event group thread number.
     * @return builder.
     */
    public Builder nettyChildEventGroupThreadNum(int nettyChildEventGroupThreadNum) {
      propHolder.nettyChildEventGroupThreadNum = nettyChildEventGroupThreadNum;
      return this;
    }

    /**
     * Set local-s3 executor thread number.
     * Default value is 4.
     *
     * @param s3ExecutorThreadNum local-s3 executor thread number.
     * @return builder.
     */
    public Builder s3ExecutorThreadNum(int s3ExecutorThreadNum) {
      propHolder.s3ExecutorThreadNum = s3ExecutorThreadNum;
      return this;
    }

    /**
     * Build a {@linkplain LocalS3} instance.
     *
     * @return created {@linkplain LocalS3} instance.
     */
    public LocalS3 build() {
      LocalS3 localS3 = new LocalS3();
      for (Field field : FieldUtils.getAllFields(LocalS3.class)) {
        if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
          continue;
        }

        try {
          field.setAccessible(true);
          Object value = FieldUtils.readField(field, propHolder);
          FieldUtils.writeField(field, localS3, value);
          log.debug(field.getName() + ": " + value);
        } catch (IllegalAccessException e) {
          throw new IllegalStateException(e);
        }
      }
      return localS3;
    }

    private int findFreeTcpPort() {
      int freePort;
      try (ServerSocket serverSocket = new ServerSocket(0)) {
        freePort = serverSocket.getLocalPort();
      } catch (IOException e) {
        throw new IllegalStateException("TCP port is not available.");
      }
      return freePort;
    }

  }

}
