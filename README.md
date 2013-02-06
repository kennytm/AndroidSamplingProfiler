AndroidSamplingProfiler
=======================

> *— Exposing Dalvik's sampling profiler to general public.*

Quick start
-----------

### Use for profiling a section of code intrusively

```java
// Profile only this thread, store result into "/sdcard".
SamplingProfilerReceiver.setStorageDirectory("/sdcard");
SamplingProfilerReceiver.start(Thread.currentThread());

...

SamplingProfilerReceiver.stop("MyProcess");
```

### Profiling code from outside


```java
public final class TestActivity extends Activity {
    private final SamplingProfilerReceiver mReceiver = new SamplingProfilerReceiver();

    ...

    @Override
    public void onDestroy() {
        ...
        mReceiver.unregister(this);
        super.onDestroy();
    }

    @Override
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        mReceiver.register(this);
        ...
    }
}
```

To control it:

```bash
# start
adb shell am broadcast -a hihex.samplingprofiler -e action start -e interval 30 -e depth 16

# stop
adb shell am broadcast -a hihex.samplingprofiler -e action stop -e format ascii

# suspend
adb shell am broadcast -a hihex.samplingprofiler -e action suspend
```

License
-------

GPLv3.


