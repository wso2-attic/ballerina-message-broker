# Messaging Metrics

Messaging metrics are useful for assessing the status of the running broker node. Message broker currently supports 
following messaging metrics.

- Total number of messages held in memory
- Total number of messages published to node
- Global message publishing rate
- Total number of message acknowledgments
- Global message acknowledging rate
- Total number of message rejections
- Global message rejection rate
- Total number of open AMQP channels
- Total number of open AMQP connection
- Total number of active consumers
- Database message read latency
- Database message read rate
- Database message read count
- Database message write latency
- Database message write rate
- Database message write count
- Database message delete latency
- Database message delete rate
- Database message delete count

## Configuring messaging metrics

Configuration related to messaging metrics are defined under the namespace `wso2.metrics`. You can find different 
options related to [reservoirs](http://metrics.dropwizard.io/3.1.0/manual/core/#histograms) and 
[reporting](http://metrics.dropwizard.io/3.1.0/manual/core/#reporters) under this section. Following is the complete 
available configuration set.

```yaml
wso2.metrics:
  # Enable Metrics
  enabled: true

  # Metrics JMX Configuration
  jmx:
    # Register MBean when initializing Metrics
    registerMBean: true
    # MBean Name
    name: org.wso2.carbon:type=Metrics

  reservoir:
    # Reservoir Type used for Histogram and Timer
    # Available types are EXPONENTIALLY_DECAYING, UNIFORM, SLIDING_WINDOW, SLIDING_TIME_WINDOW & HDR_HISTOGRAM
    type: EXPONENTIALLY_DECAYING
    # Parameters for reservoir implementations
    parameters:
      # The number of measurements to store in SLIDING_WINDOW reservoir or
      # number of samples to keep in UNIFORM reservoir
      size: 1028
      # The window of time in SLIDING_TIME_WINDOW reservoir
      window: 1
      # The unit of window in SLIDING_TIME_WINDOW reservoir. See java.util.concurrent.TimeUnit
      windowUnit: HOURS
      # The precision to use in the Recorder to be used in HDR_HISTOGRAM reservoir
      numberOfSignificantValueDigits: 2
      # Reset the HdrHistogram when taking a snapshot
      resetOnSnapshot: false

  # Metrics reporting configurations
  # The JMX, Console, CSV and SLF4J reporters are in the Metrics Core feature.
  reporting:
    jmx:
      - # The name for the JMX Reporter
        name: JMX

        # Enable JMX Reporter
        enabled: true

    console:
      - # The name for the Console Reporter
        name: Console

        # Enable Console Reporter
        enabled: true

        # Polling Period in seconds.
        # This is the period for polling metrics from the metric registry and printing in the console
        pollingPeriod: 5

    csv:
      - # The name for the CSV Reporter
        name: CSV

        # Enable CSV Reporter
        enabled: true

        # The location for CSV files. There is a CSV file for each metric.
        location: ${carbon.home}/wso2/${sys:wso2.runtime}/logs/metrics/

        # Polling Period in seconds.
        # This is the period for polling metrics from the metric registry and update CSV files in the given location
        pollingPeriod: 10

    slf4j:
      - # The name for the SLF4J Reporter
        name: SLF4J

        # Enable SLF4J Reporter
        enabled: true

        # The Logger name
        loggerName: metrics

        # The Marker name
        markerName: metrics

        # Polling Period in seconds.
        # This is the period for polling metrics from the metric registry and logging to the configured logger
        pollingPeriod: 15
```
 
These configs are extracted from the
[carbon-config repo](https://github.com/wso2/carbon-metrics/blob/v2.3.7/distribution/deployment.yaml). 



### Accessing JMX Remotely

If you need to access JMX interface remotely you will have to add following JVM parameters in the 'broker' file
where we have defined the Java command to run broker. Please note that you have to use the machine IP instead of 
`127 .0.0.1`. 

```bash
   -Djava.rmi.server.hostname=127.0.0.1 \
   -Dcom.sun.management.jmxremote.port=9595 \
   -Dcom.sun.management.jmxremote.ssl=false \
   -Dcom.sun.management.jmxremote.authenticate=false \
```
