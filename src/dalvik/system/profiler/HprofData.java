/*
 * Copyright (C) 2010 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package dalvik.system.profiler;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents sampling profiler data. Can be converted to ASCII or binary hprof-style output using
 * {@link AsciiHprofWriter} or {@link BinaryHprofWriter}.
 * <p>
 * The data includes:
 * <ul>
 * <li>the start time of the last sampling period
 * <li>the history of thread start and end events
 * <li>stack traces with frequency counts
 * <ul>
 */
public final class HprofData {

    public static enum ThreadEventType {
        START, END
    };

    /**
     * ThreadEvent represents thread creation and death events for reporting. It provides a record of the thread and
     * thread group names for tying samples back to their source thread.
     */
    public static final class ThreadEvent {
        public final ThreadEventType type;
        public final int objectId;
        public final int threadId;
        public final String threadName;
        public final String groupName;
        public final String parentGroupName;

        private ThreadEvent(final ThreadEventType type, final int threadId) {
            throw new RuntimeException("Stub!");
        }

        public static ThreadEvent start(final int objectId,
                                        final int threadId,
                                        final String threadName,
                                        final String groupName,
                                        final String parentGroupName) {
            throw new RuntimeException("Stub!");
        }

        public static ThreadEvent end(final int threadId) {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * A unique stack trace for a specific thread.
     */
    public static final class StackTrace {

        public final int stackTraceId;

        public StackTrace(final int stackTraceId, final int threadId, final StackTraceElement[] stackFrames) {
            throw new RuntimeException("Stub!");
        }

        public int getThreadId() {
            throw new RuntimeException("Stub!");
        }

        public StackTraceElement[] getStackFrames() {
            throw new RuntimeException("Stub!");
        }
    }

    /**
     * A read only container combining a stack trace with its frequency.
     */
    public static final class Sample {
        public final StackTrace stackTrace;
        public final int count;

        private Sample(final StackTrace stackTrace, final int count) {
            throw new RuntimeException("Stub!");
        }
    }

    public HprofData(final Map<StackTrace, int[]> stackTraces) {
        throw new RuntimeException("Stub!");
    }

    /**
     * The start time in milliseconds of the last profiling period.
     */
    public long getStartMillis() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Set the time for the start of the current sampling period.
     */
    public void setStartMillis(final long startMillis) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Get the {@link BinaryHprof.ControlSettings} flags
     */
    public int getFlags() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Set the {@link BinaryHprof.ControlSettings} flags
     */
    public void setFlags(final int flags) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Get the stack sampling depth
     */
    public int getDepth() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Set the stack sampling depth
     */
    public void setDepth(final int depth) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Return an unmodifiable history of start and end thread events.
     */
    public List<ThreadEvent> getThreadHistory() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Return a new set containing the current sample data.
     */
    public Set<Sample> getSamples() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Record an event in the thread history.
     */
    public void addThreadEvent(final ThreadEvent event) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Record an stack trace and an associated int[] cell of sample cound for the stack trace. The caller is allowed
     * retain a pointer to the cell to update the count. The SamplingProfiler intentionally does not present a mutable
     * view of the count.
     */
    public void addStackTrace(final StackTrace stackTrace, final int[] countCell) {
        throw new RuntimeException("Stub!");
    }
}
