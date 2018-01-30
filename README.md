# fluent-benchmark-client

Benchmark tool for Fluentd.

## Build

```
$ ./gradlew shadowJar
```

## Run

```
$ cd /path/to/fluent-benchmark-client
$ java -Dlog4j.configurationFile=file://$(pwd)/config/log4j2.yaml \
       -jar ./build/libs/fluent-benchmark-client-1.0-SNAPSHOT-all.jar \
       --max-buffer-size=4g --period=60s --n-events=6000000
```

Emit 6000000 events in 60 seconds.

