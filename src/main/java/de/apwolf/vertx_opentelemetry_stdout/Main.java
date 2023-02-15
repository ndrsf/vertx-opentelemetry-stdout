package de.apwolf.vertx_opentelemetry_stdout;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.tracing.opentelemetry.OpenTelemetryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class Main {

  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    configureLogging();

    LOGGER.info("Starting up");
    VertxOptions vertxOptions = new VertxOptions();
    OpenTelemetry openTelemetry = configureOpenTracing();
    vertxOptions.setTracingOptions(new OpenTelemetryOptions(openTelemetry));

    Vertx vertx = Vertx.vertx(vertxOptions);
    vertx.deployVerticle(new MainVerticle());
  }

  /**
   * Configures jul-to-slf4j to have consistent logging
   */
  private static void configureLogging() {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  /**
   *
   * @return a preconfigured OpenTelemetry object which can be used to pass to Vertx
   */
  private static OpenTelemetry configureOpenTracing() {
    SdkTracerProvider sdkTracerProvider = SdkTracerProvider
      .builder()
      .addSpanProcessor(BatchSpanProcessor.builder(LoggingSpanExporter.create()).build())
      .build();

    return OpenTelemetrySdk
      .builder()
      .setTracerProvider(sdkTracerProvider)
      .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
      .buildAndRegisterGlobal();
  }
}
