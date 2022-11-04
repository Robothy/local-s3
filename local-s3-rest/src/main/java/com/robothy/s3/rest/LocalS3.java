package com.robothy.s3.rest;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.robothy.netty.initializer.HttpServerInitializer;
import com.robothy.s3.core.service.BucketService;
import com.robothy.s3.core.service.ObjectService;
import com.robothy.s3.core.service.manager.LocalS3Manager;
import com.robothy.s3.rest.handler.LocalS3RouterFactory;
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
import java.nio.file.Path;
import java.util.Objects;
import javax.xml.stream.XMLInputFactory;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * local-s3 service starter.
 */
@Slf4j
public class LocalS3 {

  /* Configurations */
  @Getter
  private int port = 8080;

  @Getter
  private Path dataDirectory;

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
   * @return a new {@linkplain Builder} instacne.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Startup the local-s3 service.
   */
  @SneakyThrows
  public void start() {
    registerServices();

    this.parentGroup = new NioEventLoopGroup(nettyParentEventGroupThreadNum);
    this.childGroup = new NioEventLoopGroup(nettyChildEventGroupThreadNum);
    this.executorGroup = new DefaultEventLoopGroup(s3ExecutorThreadNum);

    ServerBootstrap serverBootstrap = new ServerBootstrap();
    ChannelFuture channelFuture = serverBootstrap.group(parentGroup, childGroup)
        .handler(new LoggingHandler(LogLevel.DEBUG))
        .channel(NioServerSocketChannel.class)
        .childHandler(new HttpServerInitializer(executorGroup, LocalS3RouterFactory.create()))
        .bind(port)
        .sync();
    log.info("LocalS3 started.");
    this.serverSocketChannel = channelFuture.channel();
  }

  private void registerServices() {
    LocalS3Manager manager = Objects.nonNull(dataDirectory) ?
        LocalS3Manager.createFileSystemS3Manager(dataDirectory) :
        LocalS3Manager.createInMemoryS3Manager();
    BucketService bucketService = manager.bucketService();
    ObjectService objectService = manager.objectService();
    ServiceFactory.register(BucketService.class, () -> bucketService);
    ServiceFactory.register(ObjectService.class, () -> objectService);

    XMLInputFactory input = new WstxInputFactory();
    input.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
    XmlMapper xmlMapper = new XmlMapper(new XmlFactory(input, new WstxOutputFactory()));
    xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    ServiceFactory.register(XmlMapper.class, () -> xmlMapper);
  }

  /**
   * Shutdown the local-s3 service.
   */
  public void shutdown() {
    if (null == this.parentGroup || null == this.childGroup) {
      throw new IllegalStateException("LocalS3 is not started.");
    }

    try {
      this.serverSocketChannel.close().sync();
      log.info("LocalS3 stopped.");
    } catch (InterruptedException e) {
      log.error("Close server socket channel failed.", e);
    } finally {
      executorGroup.shutdownGracefully();
      childGroup.shutdownGracefully();
      parentGroup.shutdownGracefully();
    }
  }

  public static class Builder {

    private final LocalS3 propHolder = new LocalS3();

    /**
     * Set the port that local-s3 service listen to.
     * Default port is 8080.
     *
     * @param port customized port.
     * @return builder.
     */
    public Builder port(int port) {
      propHolder.port = port;
      return this;
    }

    /**
     * Set the local-s3 data directory. The value is {@code null} by default,
     * while data is stored in Java Heap. If the path is specified, s3-local
     * service load data from and store data into this directory.
     *
     *
     * @param dataDirectory data directory.
     * @return builder.
     */
    public Builder dataDirectory(Path dataDirectory) {
      propHolder.dataDirectory = dataDirectory;
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
      localS3.port = propHolder.port;
      localS3.dataDirectory = propHolder.dataDirectory;
      localS3.nettyParentEventGroupThreadNum = propHolder.nettyParentEventGroupThreadNum;
      localS3.nettyChildEventGroupThreadNum = propHolder.nettyChildEventGroupThreadNum;
      localS3.s3ExecutorThreadNum = propHolder.s3ExecutorThreadNum;
      return localS3;
    }

  }

}
