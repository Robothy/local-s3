package com.robothy.s3.testcontainer;

import java.io.IOException;
import java.net.ServerSocket;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class LocalS3Container extends GenericContainer<LocalS3Container> {

  public static final String IMAGE_NAME = "luofuxiang/local-s3";

  public static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse(IMAGE_NAME);

  private int port;

  private String dataPath;

  private Mode mode;

  /**
   * Construct a {@linkplain LocalS3Container} with specified {@linkplain DockerImageName}.
   *
   * @param dockerImageName the docker image to run.
   */
  LocalS3Container(DockerImageName dockerImageName) {
    super(dockerImageName);
    dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);
    this.waitingFor(Wait.forLogMessage("^.{1,}LocalS3 started.\n$", 1));
  }

  /**
   * Construct a {@linkplain LocalS3Container} instance with specified tag.
   *
   * @param tag the Docker image tag.
   */
  public LocalS3Container(String tag) {
    this(DockerImageName.parse(IMAGE_NAME).withTag(tag));
  }

  /**
   * Bind the specified host port to container port 80.
   *
   * @param port host port.
   * @return this.
   */
  public LocalS3Container withHttpPort(int port) {
    this.port = port;
    super.addFixedExposedPort(port, 80);
    return this;
  }

  /**
   * Bind a random TCP port to container port 80.
   * Call {@linkplain #getPort()} to get the real bound port.
   *
   * @return this.
   */
  public LocalS3Container withRandomHttpPort() {
    return withHttpPort(findFreeTcpPort());
  }

  /**
   * Bind a host directory to LocalS3 data path.
   *
   * @param path host path.
   * @return this.
   */
  public LocalS3Container withDataPath(String path) {
    return super.withFileSystemBind(path, "/data");
  }

  /**
   * Set the LocalS3 mode. i.e. Set the environment variable "MODE" when starting a container.
   *
   * @param mode {@linkplain Mode}.
   * @return this.
   */
  public LocalS3Container withMode(Mode mode) {
    return super.withEnv("MODE", mode.name());
  }

  /**
   * Get the actual bound port of LocalS3 container.
   * Call {@linkplain #withHttpPort(int)} or {@linkplain #withRandomHttpPort()}
   * to bind a host port to container port 80.
   *
   * @return the actual bound port of the container; or {@code 0} if the
   * container doesn't bound a host port.
   */
  public int getPort() {
    return port;
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

  /**
   * LocalS3 mode.
   */
  public enum Mode {

    PERSISTENCE,

    IN_MEMORY

  }

}
