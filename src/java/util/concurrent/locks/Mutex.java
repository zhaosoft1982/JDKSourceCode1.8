package java.util.concurrent.locks;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.TimeUnit;

/**
 * Mutex是一个不可重入的互斥锁实现实例。
 * 锁资源（AQS里的state）只有两种状态：0表示未锁定，1表示锁定。
 *
 * @author xiaoleizhao
 * @create 2019-12-13 14:23
 **/
public class Mutex implements Lock, java.io.Serializable {


    /**
     * Our internal helper class
     * 自定义同步器
     */
    private static class Sync extends AbstractQueuedSynchronizer {


        /**
         * Reports whether in locked state
         * 判断是否锁定状态
         *
         * @return
         */
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }


        /**
         * Acquires the lock if state is zero
         * 尝试获取资源，立即返回。成功则返回true，否则false。
         *
         * @param acquires
         * @return
         */
        public boolean tryAcquire(int acquires) {
            // 这里限定只能为1个量
            // Otherwise unused
            assert acquires == 1;
            //state为0才设置为1，不可重入！
            if (compareAndSetState(0, 1)) {
                //设置为当前线程独占资源
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        /**
         * Releases the lock by setting state to zer
         * 尝试释放资源，立即返回。成功则为true，否则false。
         *
         * @param releases
         * @return
         */
        protected boolean tryRelease(int releases) {
            // Otherwise unused
            assert releases == 1;
            //既然来释放，那肯定就是已占有状态了。只是为了保险，多层判断！
            if (getState() == 0) throw new IllegalMonitorStateException();
            setExclusiveOwnerThread(null);
            //释放资源，放弃占有状态
            setState(0);
            return true;
        }

        // Provides a Condition
        Condition newCondition() {
            return new ConditionObject();
        }

        // Deserializes properly
        private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
            s.defaultReadObject();
            // reset to unlocked state
            setState(0);
        }
    }


    /**
     * The sync object does all the hard work. We just forward to it.
     * 真正同步类的实现都依赖继承于AQS的自定义同步器！
     */
    private final Sync sync = new Sync();

    public void lock() {
        sync.acquire(1);
    }

    /**
     * tryLock<-->tryAcquire。两者语义一样：尝试获取资源，要求立即返回。成功则为true，失败则为false。
     *
     * @return
     */
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    /**
     * unlock<-->release。两者语文一样：释放资源。
     */
    public void unlock() {
        sync.release(1);
    }

    public Condition newCondition() {
        return sync.newCondition();
    }


    /**
     * 锁是否占有状态
     *
     * @return
     */
    public boolean isLocked() {
        return sync.isHeldExclusively();
    }

    /**
     * 队列中是否有等待的线程
     *
     * @return
     */
    public boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }
}

