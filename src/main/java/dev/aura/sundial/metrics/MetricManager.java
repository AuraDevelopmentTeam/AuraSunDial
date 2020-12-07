package dev.aura.sundial.metrics;

import lombok.experimental.UtilityClass;
import org.bstats.sponge.Metrics2;

@UtilityClass
public class MetricManager {
  public static void startMetrics(Metrics2.Factory metricsFactory) {
    Metrics2 metrics = metricsFactory.make(1534);

    metrics.addCustomChart(new LanguageData());
  }
}
