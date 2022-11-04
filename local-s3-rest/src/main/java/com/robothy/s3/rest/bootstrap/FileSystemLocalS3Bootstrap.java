package com.robothy.s3.rest.bootstrap;

import com.robothy.netty.initializer.HttpServerInitializer;
import com.robothy.s3.rest.config.ConfigNames;
import com.robothy.s3.rest.config.PersistLocalS3Options;
import com.robothy.s3.rest.handler.LocalS3RouterFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import java.util.Properties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class FileSystemLocalS3Bootstrap implements LocalS3Bootstrap {


  private final PersistLocalS3Options options;

  private NioEventLoopGroup parentGroup;

  private NioEventLoopGroup childGroup;

  private EventExecutorGroup executorGroup;

  private Channel serverSocketChannel;

  FileSystemLocalS3Bootstrap(Properties config) {
    this.options = buildOptions(config);
  }


  @Override
  @SneakyThrows
  public void start() {
    this.parentGroup = new NioEventLoopGroup(options.getNettyParentEventGroupThreadNum());
    this.childGroup = new NioEventLoopGroup(options.getNettyChildEventGroupThreadNum());
    this.executorGroup = new DefaultEventLoopGroup(options.getS3ExecutorThreadNum());

    ServerBootstrap serverBootstrap = new ServerBootstrap();
    ChannelFuture channelFuture = serverBootstrap.group(parentGroup, childGroup)
        .handler(new LoggingHandler(LogLevel.DEBUG))
        .channel(NioServerSocketChannel.class)
        .childHandler(new HttpServerInitializer(executorGroup, LocalS3RouterFactory.create()))
        .bind(options.getPort())
        .sync();
    this.serverSocketChannel = channelFuture.channel();
  }

  @Override
  public void shutdown() {
    if (null == this.parentGroup || null == this.childGroup) {
      throw new IllegalStateException("LocalS3 not started.");
    }

    try {
      this.serverSocketChannel.close().sync();
    } catch (InterruptedException e) {
      log.error("Close server socket channel failed.", e);
    } finally {
      executorGroup.shutdownGracefully();
      childGroup.shutdownGracefully();
      parentGroup.shutdownGracefully();
    }
  }

  private PersistLocalS3Options buildOptions(Properties config) {
    int port = Integer.parseInt(getProp(config, ConfigNames.PORT, "80"));
    int parentThreadNum = Integer.parseInt(getProp(config, ConfigNames.NETTY_PARENT_EVENT_LOOP_GROUP_THREAD, "0"));
    int childThreadNum = Integer.parseInt(getProp(config, ConfigNames.NETTY_CHILD_EVENT_LOOP_GROUP_THREAD, "0"));
    int executorThreadNum = Integer.parseInt(getProp(config, ConfigNames.EXECUTOR_THREAD, "5"));
    String rootDir = (String) config.getOrDefault(ConfigNames.ROOT_DIRECTORY, System.getProperty("user.home") + "/s3");
    log.info(ConfigNames.PORT + ": {}", port);
    log.info(ConfigNames.EXECUTOR_THREAD + ": {}", executorThreadNum);
    log.info(ConfigNames.ROOT_DIRECTORY + ": {}", rootDir);
    return PersistLocalS3Options.builder().port(port)
        .nettyParentEventGroupThreadNum(parentThreadNum)
        .nettyChildEventGroupThreadNum(childThreadNum)
        .s3ExecutorThreadNum(executorThreadNum)
        .dataPath(rootDir)
        .build();
  }

  private String getProp(Properties config, String name, String defaultValue) {
    String value = config.containsKey(name) ? config.get(name).toString() : null;
    if (value == null) {
      value = config.getProperty(name);
    }
    return value == null ? defaultValue : value;
  }

}
