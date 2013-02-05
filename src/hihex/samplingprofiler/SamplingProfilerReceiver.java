package hihex.samplingprofiler;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;
import android.util.Log;

import dalvik.system.profiler.AsciiHprofWriter;
import dalvik.system.profiler.SamplingProfiler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class SamplingProfilerReceiver extends BroadcastReceiver {
    private static File sStorageDirectory = new File("/data/snapshots");
    private static SamplingProfiler sProfiler = null;

    /**
     * The default sampling interval.
     */
    public static final int kDefaultInterval = 100;

    /**
     * The default maximum sampling depth.
     */
    public static final int kDefaultDepth = 16;

    /**
     * Set the storage directory. New profile results will be written to this directory. The default directory is
     * {@code /data/snapshots} when used statically, or {@link Context#getCacheDir()} when called from
     * {@link #register(Context)}.
     * 
     * @param newDirectory
     *            The directory to write the files to. This directory must already exist and writable by the profiled
     *            application.
     */
    public static void setStorageDirectory(final File newDirectory) {
        sStorageDirectory = newDirectory;
    }

    /**
     * Set the storage directory. New profile results will be written to this directory. The default directory is
     * {@code /data/snapshots} when used statically, or {@link Context#getCacheDir()} when called from
     * {@link #register(Context)}.
     * 
     * @param newDirectory
     *            The directory to write the files to. This directory must already exist and writable by the profiled
     *            application.
     */
    public static void setStorageDirectory(final String newDirectory) {
        sStorageDirectory = new File(newDirectory);
    }

    /**
     * Start profiling the thread group the current thread belongs to.
     */
    public static void start() {
        start(kDefaultInterval, kDefaultDepth, Thread.currentThread().getThreadGroup());
    }

    /**
     * Start profiling the thread group the current thread belongs to.
     */
    public static void start(final int interval) {
        start(interval, kDefaultDepth, Thread.currentThread().getThreadGroup());
    }

    /**
     * Start profiling all threads in the provided thread group.
     * 
     * @param period
     *            The sampling period.
     * @param depth
     *            The maximum depth of the stack trace. <strong>Do not pass in {@link Integer#MAX_VALUE} here</strong>,
     *            because Dalvik by default will preallocate arrays of size up to this value.
     * @param threadGroup
     *            The thread group to sample.
     */
    public static void start(final int period, final int depth, final ThreadGroup threadGroup) {
        if (sProfiler != null) {
            Log.w("SamplingProfilerReceiver", "Profiler already started!");
        } else {
            sProfiler = new SamplingProfiler(depth, SamplingProfiler.newThreadGroupTheadSet(threadGroup));
        }
        resume(period);
    }

    /**
     * Resume sampling.
     * 
     * @param period
     *            The sampling period.
     */
    public static void resume(final int period) {
        Log.w("SamplingProfilerReceiver", "Starting/resuming profiler...");
        sProfiler.start(period);
    }

    /**
     * Suspend (pause) sampling.
     */
    public static void suspend() {
        Log.w("SamplingProfilerReceiver", "Stopping/suspending profiler...");
        sProfiler.stop();
    }

    /**
     * Stop sampling and write the data into the storage directory.
     * 
     * @return The output file.
     */
    public static File stop(final Context context) {
        if (sProfiler == null) {
            Log.w("SamplingProfilerReceiver", "Profiler not started!");
            return null;
        }

        sProfiler.stop();

        try {
            final ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            String processName = null;
            final int myPid = Process.myPid();
            for (final RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
                if (processInfo.pid == myPid) {
                    processName = processInfo.processName;
                    break;
                }
            }

            final File outputPath = File.createTempFile(processName + "-", ".snapshot", sStorageDirectory);
            outputPath.setReadable(/*readable*/true, /*ownerOnly*/false);

            final BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(outputPath));
            try {
                AsciiHprofWriter.write(sProfiler.getHprofData(), stream);
            } finally {
                stream.close();
            }

            Log.i("SamplingProfiler", "Written profile to " + outputPath);
            return outputPath;
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            sProfiler.shutdown();
            sProfiler = null;
        }
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (action.equals("hihex.samplingprofiler.START")) {
            start();
        } else if (action.equals("hihex.samplingprofiler.STOP")) {
            final File outputPath = stop(context);
            setResultData(outputPath.getAbsolutePath());
        } else {
            return;
        }
    }

    public void register(final Context context) {
        final IntentFilter filter = new IntentFilter();
        filter.addAction("hihex.samplingprofiler.START");
        filter.addAction("hihex.samplingprofiler.STOP");
        context.registerReceiver(this, filter);
        setStorageDirectory(context.getCacheDir());
    }

    public void unregister(final Context context) {
        context.unregisterReceiver(this);
    }
}
