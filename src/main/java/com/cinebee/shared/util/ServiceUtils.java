package com.cinebee.shared.util;

import com.cinebee.shared.exception.ApiException;
import com.cinebee.shared.exception.ErrorCode;

import java.util.Optional;
import java.util.function.Supplier;

public class ServiceUtils {

    public static <T> T findObjectOrThrow(Supplier<Optional<T>> supplier, ErrorCode errorCode) {
        return supplier.get().orElseThrow(() -> new ApiException(errorCode));
    }
}

