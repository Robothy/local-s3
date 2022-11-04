package com.robothy.s3.core.service;

import com.robothy.s3.core.storage.Storage;

/**
 * Represent services that perform operations on a {@linkplain Storage}.
 */
public interface StorageApplicable {

  Storage storage();

}
