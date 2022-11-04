package com.robothy.s3.rest.model.request;

import java.io.InputStream;
import lombok.Data;

/**
 * Decoded Amazon request body.
 */
@Data
public class DecodedAmzRequestBody {

  /**
   * Represents decoded input stream.
   */
  private InputStream decodedBody;

  /**
   * Represents decoded content length.
   */
  private long decodedContentLength;

}
