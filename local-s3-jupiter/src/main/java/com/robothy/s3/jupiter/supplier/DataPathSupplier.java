package com.robothy.s3.jupiter.supplier;

import com.robothy.s3.jupiter.LocalS3;
import java.util.function.Supplier;

/**
 * A supplies that provide {@linkplain LocalS3} data path. The data path supplier
 * implementation class must have a no-args constructor.
 */
public interface DataPathSupplier extends Supplier<String> {

}
