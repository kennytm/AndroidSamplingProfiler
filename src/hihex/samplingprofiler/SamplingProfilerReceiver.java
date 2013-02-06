package hihex.samplingprofiler;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;

import dalvik.system.profiler.AsciiHprofWriter;
import dalvik.system.profiler.BinaryHprofWriter;
import dalvik.system.profiler.HprofData;
import dalvik.system.profiler.SamplingProfiler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A broadcast receiver that enables the Dalvik sampling profiler.
 * 
 * <h2>Use for profiling a section of code intrusively</h2>
 * 
 * <p>
 * One could surround a piece of code for profiling with one of the {@link #start} and {@link #stop} overloads.
 * </p>
 * 
 * <pre>
 * // Profile only this thread, store result into "/sdcard".
 * SamplingProfilerReceiver.setStorageDirectory("/sdcard");
 * SamplingProfilerReceiver.start(Thread.currentThread());
 * 
 * ...
 * 
 * SamplingProfilerReceiver.stop("MyProcess");
 * </pre>
 * 
 * <h2>Profiling code from outside</h2>
 * 
 * <p>
 * One could also register an instance of this class as a BroadcastReceiver, and use ADB to start/stop profiling.
 * </p>
 * 
 * <pre>
 * public final class TestActivity extends Activity {
 *     private final SamplingProfilerReceiver mReceiver = new SamplingProfilerReceiver();
 * 
 *     ...
 * 
 *     &#64;Override 
 *     public void onDestroy() {
 *         ...
 *         mReceiver.unregister(this);
 *         super.onDestroy();
 *     }
 * 
 *     &#64;Override
 *     public void onCreate(final Bundle bundle) {
 *         super.onCreate(bundle);
 *         mReceiver.register(this);
 *         ...
 *     }
 * }
 * </pre>
 * 
 * @author kennytm
 */
public final class SamplingProfilerReceiver extends BroadcastReceiver {
    private static File sStorageDirectory = new File("/data/snapshots");
    private static SamplingProfiler sProfiler = null;
    private static final String kTag = "SamplingProfiler";
    private static final String kIntentAction = "hihex.samplingprofiler";

    /**
     * The default sampling interval.
     */
    public static final int kDefaultInterval = 30;

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
        start(kDefaultInterval);
    }

    /**
     * Start profiling the thread group the current thread belongs to.
     */
    public static void start(final int interval) {
        start(interval, kDefaultDepth, Thread.currentThread().getThreadGroup());
    }

    /**
     * Start profiling a list of threads.
     */
    public static void start(final Thread... threads) {
        start(kDefaultInterval, threads);
    }

    /**
     * Start profiling a list of threads.
     */
    public static void start(final int interval, final Thread... threads) {
        start(interval, kDefaultDepth, SamplingProfiler.newArrayThreadSet(threads));
    }

    /**
     * Start profiling all threads in the provided thread group.
     * 
     * @param interval
     *            The sampling interval.
     * @param depth
     *            The maximum depth of the stack trace. <strong>Do not pass in {@link Integer#MAX_VALUE} here</strong>,
     *            because Dalvik by default will preallocate arrays of size up to this value.
     * @param threadGroup
     *            The thread group to sample.
     */
    public static void start(final int interval, final int depth, final ThreadGroup threadGroup) {
        start(interval, kDefaultDepth, SamplingProfiler.newThreadGroupTheadSet(Thread.currentThread().getThreadGroup()));
    }

    private static void start(final int interval, final int depth, final SamplingProfiler.ThreadSet threadSet) {
        Log.i(kTag, "Starting/resuming profiler...");
        if (sProfiler == null) {
            sProfiler = new SamplingProfiler(depth, threadSet);
        }
        sProfiler.start(interval);
    }

    /**
     * Suspend (pause) sampling.
     */
    public static void suspend() {
        if (sProfiler == null) {
            return;
        }
        Log.i("SamplingProfilerReceiver", "Suspending profiler...");
        sProfiler.stop();
    }

    /**
     * Stop sampling and write the data into the storage directory.
     * 
     * @param context
     *            The context for automatically fetching the process name.
     * 
     * @return The output file.
     */
    public static File stop(final Context context) {
        return stop(context, /*isBinary*/false);
    }

    /**
     * Stop sampling and write the data into the storage directory.
     * 
     * @param context
     *            The context for automatically fetching the process name.
     * @param isBinary
     *            whether we write the *.hprof file as binary or ASCII format. The default is ASCII.
     * 
     * @return The output file.
     */
    public static File stop(final Context context, final boolean isBinary) {
        final ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String processName = null;
        final int myPid = Process.myPid();
        for (final RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == myPid) {
                processName = processInfo.processName;
                break;
            }
        }

        return stop(processName, isBinary);
    }

    /**
     * Stop sampling and write the data into the storage directory.
     * 
     * @return The output file.
     */
    public static File stop(final String processName, final boolean isBinary) {
        if (sProfiler == null) {
            Log.w(kTag, "Profiler not started!");
            return null;
        }

        sProfiler.stop();

        try {
            final File outputPath = File.createTempFile(processName + ".", ".hprof", sStorageDirectory);
            outputPath.setReadable(/*readable*/true, /*ownerOnly*/false);

            final BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(outputPath));
            final HprofData data = sProfiler.getHprofData();
            try {
                if (isBinary) {
                    BinaryHprofWriter.write(data, stream);
                } else {
                    AsciiHprofWriter.write(data, stream);
                }
            } finally {
                stream.close();
            }

            Log.i(kTag, "Written profile to " + outputPath);
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
        final String intentAction = intent.getAction();
        if (!kIntentAction.equals(intentAction)) {
            Log.e(kTag, "Received unknown action: " + intentAction);
            setResultCode(10000);
            return;
        }

        final Bundle extras = intent.getExtras();
        if (extras == null) {
            Log.e(kTag, "Please provide an action!");
            setResultCode(10001);
            return;
        }

        final String action = extras.getString("action");
        if ("start".equals(action)) {
            final int interval = extras.getInt("interval", kDefaultInterval);
            final int depth = extras.getInt("depth", kDefaultDepth);
            start(interval, depth, Thread.currentThread().getThreadGroup());
            setResultCode(1);
        } else if ("stop".equals(action)) {
            final String directory = extras.getString("directory");
            if (directory != null) {
                setStorageDirectory(directory);
            }
            final String format = extras.getString("format");
            final boolean isBinary;
            if (format == null || "ascii".equals(format)) {
                isBinary = false;
            } else if ("binary".equals(format)) {
                isBinary = true;
            } else {
                setResultCode(10002);
                Log.e(kTag, "Unknown format: " + format);
                return;
            }
            final File outputPath = stop(context, isBinary);
            setResultData(outputPath.getAbsolutePath());
            setResultCode(2);
        } else if ("suspend".equals(action)) {
            suspend();
            setResultCode(3);
        } else {
            Log.e(kTag, "Unknown action: " + action);
            setResultCode(10004);
        }
    }

    /**
     * Register this broadcast receiver, so that it can control the sampling profiler externally.
     */
    public void register(final Context context) {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(kIntentAction);
        context.registerReceiver(this, filter);
        setStorageDirectory(context.getCacheDir());
    }

    /**
     * Unregister this broadcast receiver.
     */
    public void unregister(final Context context) {
        context.unregisterReceiver(this);
    }
}

/*

AndroidSamplingProfiler — Exposing Dalvik's sampling profiler to general public.
Copyright (C) 2013 HiHex Ltd.

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later 
version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see 
<http://www.gnu.org/licenses/>.

*/
