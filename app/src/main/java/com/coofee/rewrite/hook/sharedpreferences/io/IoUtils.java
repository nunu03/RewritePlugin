package com.coofee.rewrite.hook.sharedpreferences.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * copy from didi booster
 * @author neighbWang
 */
public final class IoUtils {

    private IoUtils() {
    }

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }
}