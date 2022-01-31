package com.distilledcourses.micrometerdemo.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.ConditionalOnEnabledMetricsExport;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore({CompositeMeterRegistryAutoConfiguration.class})
@ConditionalOnEnabledMetricsExport("cloudwatch")
public class CloudWatchMetricsExportConfig {
    @Bean
    @ConditionalOnMissingBean
    public CloudWatchMeterRegistry cloudWatchMeterRegistry(CloudWatchConfig cloudWatchConfig, Clock clock, CloudWatchAsyncClient cloudWatchAsyncClient) {
        CloudWatchMeterRegistry cloudWatchMeterRegistry = new CloudWatchMeterRegistry(cloudWatchConfig, clock, cloudWatchAsyncClient);
        cloudWatchMeterRegistry.config()
                .meterFilter(MeterFilter.denyUnless(id -> id.getName().startsWith("http")))
                .meterFilter(percentiles("http", 0.90, 0.95, 0.99));
        return cloudWatchMeterRegistry;
    }

    @Bean
    @ConditionalOnMissingBean
    public CloudWatchConfig cloudWatchConfig() {
        return new CloudWatchConfig() {
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public String namespace() {
                return "App/MicrometerDemo";
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public CloudWatchAsyncClient cloudWatchAsyncClient() {
        return CloudWatchAsyncClient.create();
    }

    static MeterFilter percentiles(String prefix, double... percentiles) {
        return new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                if (id.getType() == Meter.Type.TIMER && id.getName().startsWith(prefix)) {
                    return DistributionStatisticConfig.builder()
                            .percentiles(percentiles)
                            .build()
                            .merge(config);
                }
                return config;
            }
        };
    }
}
