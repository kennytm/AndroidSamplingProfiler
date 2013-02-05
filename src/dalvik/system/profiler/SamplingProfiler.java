package dalvik.system.profiler;

/**
 * A sampling profiler. It currently is implemented without any virtual machine support, relying solely on
 * {@code Thread.getStackTrace} to collect samples. As such, the overhead is higher than a native approach and it does
 * not provide insight into where time is spent within native code, but it can still provide useful insight into where a
 * program is spending time.
 * 
 * <h3>Usage Example</h3>
 * 
 * The following example shows how to use the {@code SamplingProfiler}. It samples the current thread's stack to a depth
 * of 12 stack frame elements over two different measurement periods with samples taken every 100 milliseconds. In then
 * prints the results in hprof format to the standard output.
 * 
 * <pre>
 * {
 *     &#064;code
 *     ThreadSet threadSet = SamplingProfiler.newArrayThreadSet(Thread.currentThread());
 *     SamplingProfiler profiler = new SamplingProfiler(12, threadSet);
 *     profiler.start(100);
 *     // period of measurement
 *     profiler.stop();
 *     // period of non-measurement
 *     profiler.start(100);
 *     // another period of measurement
 *     profiler.stop();
 *     profiler.shutdown();
 *     AsciiHprofWriter.write(profiler.getHprofData(), System.out);
 * }
 * </pre>
 */
public final class SamplingProfiler {
    /**
     * Create a sampling profiler that collects stacks with the specified depth from the threads specified by the
     * specified thread collector.
     * 
     * @param depth
     *            The maximum stack depth to retain for each sample similar to the hprof option of the same name. Any
     *            stack deeper than this will be truncated to this depth. A good starting value is 4 although it is not
     *            uncommon to need to raise this to get enough context to understand program behavior. While programs
     *            with extensive recursion may require a high value for depth, simply passing in a value for
     *            Integer.MAX_VALUE is not advised because of the significant memory need to retain such stacks and
     *            runtime overhead to compare stacks.
     * 
     * @param threadSet
     *            The thread set specifies which threads to sample. In a general purpose program, all threads typically
     *            should be sample with a ThreadSet such as provied by {@link #newThreadGroupTheadSet
     *            newThreadGroupTheadSet}. For a benchmark a fixed set such as provied by {@link #newArrayThreadSet
     *            newArrayThreadSet} can reduce the overhead of profiling.
     */
    public SamplingProfiler(final int depth, final ThreadSet threadSet) {
        throw new RuntimeException("Stub!");
    }

    /**
     * A ThreadSet specifies the set of threads to sample.
     */
    public static interface ThreadSet {
        /**
         * Returns an array containing the threads to be sampled. The array may be longer than the number of threads to
         * be sampled, in which case the extra elements must be null.
         */
        public Thread[] threads();
    }

    /**
     * Returns a ThreadSet for a fixed set of threads that will not vary at runtime. This has less overhead than a
     * dynamically calculated set, such as {@link #newThreadGroupTheadSet}, which has to enumerate the threads each time
     * profiler wants to collect samples.
     */
    public static ThreadSet newArrayThreadSet(final Thread... threads) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Returns a ThreadSet that is dynamically computed based on the threads found in the specified ThreadGroup and that
     * ThreadGroup's children.
     */
    public static ThreadSet newThreadGroupTheadSet(final ThreadGroup threadGroup) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Starts profiler sampling at the specified rate.
     * 
     * @param interval
     *            The number of milliseconds between samples
     */
    public void start(final int interval) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Stops profiler sampling. It can be restarted with {@link #start(int)} to continue sampling.
     */
    public void stop() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Shuts down profiling after which it can not be restarted. It is important to shut down profiling when done to
     * free resources used by the profiler. Shutting down the profiler also stops the profiling if that has not already
     * been done.
     */
    public void shutdown() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Returns the hprof data accumulated by the profiler since it was created. The profiler needs to be stopped, but
     * not necessarily shut down, in order to access the data. If the profiler is restarted, there is no thread safe way
     * to access the data.
     */
    public HprofData getHprofData() {
        throw new RuntimeException("Stub!");
    }
}
