# fluent-benchmark-client

Benchmark tool for Fluentd.

## Build and install

```
$ ./gradlew installDist
```

## Run

```
$ cd /path/to/fluent-benchmark-client
$ ./build/install/fluent-benchmark-client/bin/fluent-benchmark-client \
  --max-buffer-size=4g --period=60s --n-events=6000000
```

Emit 6000000 events in 60 seconds.

## How to release

Build and install:

```
$ ./gradlew installDist
```

Create artifacts:

```
$ ./gradlew
```

Create tag:

```
$ ./gradlew createTag
```

Push tags:

```
$ ./gradlew pushTag
```

And then upload archives to GitHub releases.
