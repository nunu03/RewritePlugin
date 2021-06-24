package com.coofee.rewrite.hook;

import android.os.Process;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class HookExecutors {

    public static Executor newOptimizedSingleThreadExecutor(final String name, int priority, boolean allowCoreThreadTimeOut) {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(name, priority));
        executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        return executor;
    }

    private static class NamedThreadFactory extends AtomicLong implements ThreadFactory {
        private static final long serialVersionUID = -1401163056383184497L;
        private final String mPrefix;
        private final int mPriority;

        public NamedThreadFactory(String name, int priority) {
            this.mPrefix = name;
            this.mPriority = priority;
        }

        public Thread newThread(Runnable r) {
            return new Thread(r, this.mPrefix + " #" + this.incrementAndGet()) {
                public void run() {
                    Process.setThreadPriority(mPriority);
                    super.run();
                }
            };
        }
    }

}
