package fs.client.async;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The daemon factory.
 *
 * This daemon factory ensures that if the main thread terminates, the application is considered 'done' and will not
 * wait for these threads to terminate.
 */
public final class DaemonThreadFactory implements ThreadFactory {
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public DaemonThreadFactory(String namePrefix) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + "-thread-" + threadNumber.getAndIncrement(), 0);
        t.setDaemon(true);
        return t;
    }
}
