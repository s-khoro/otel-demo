package org.example.config;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.otel.bridge.*;
import io.micrometer.tracing.propagation.Propagator;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

import java.util.Collections;

public class OtelNativeConfiguration {

    public static OpenTelemetrySdk buildSdk() {
        // span exporter
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint("http://localhost:4317")
                .build();

        // common span resources
        Resource extraResources = Resource.create(Attributes.builder()
                .put(ResourceAttributes.SERVICE_NAME, "native-service")
                .build());

        // trace provider
        // batching parameters
        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .setSampler(Sampler.alwaysOn())
                .setResource(Resource.getDefault().merge(extraResources))
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                // propagators used for tracing context importing/exporting
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();
    }

    public static io.opentelemetry.api.trace.Tracer buildRawTracer(OpenTelemetrySdk openTelemetrySdk) {
        return openTelemetrySdk.getTracerProvider()
                .get("io.micrometer.micrometer-tracing");
    }

    public static Tracer buildTracer(io.opentelemetry.api.trace.Tracer rawTracer) {
        OtelCurrentTraceContext otelCurrentTraceContext = new OtelCurrentTraceContext();
        Slf4JEventListener slf4JEventListener = new Slf4JEventListener();
        Slf4JBaggageEventListener slf4JBaggageEventListener = new Slf4JBaggageEventListener(Collections.emptyList());

        return new OtelTracer(rawTracer, otelCurrentTraceContext, event -> {
            slf4JEventListener.onEvent(event);
            slf4JBaggageEventListener.onEvent(event);
        }, new OtelBaggageManager(otelCurrentTraceContext, Collections.emptyList(), Collections.emptyList()));
    }

    public static Propagator propagator(OpenTelemetrySdk openTelemetrySdk,
                                        io.opentelemetry.api.trace.Tracer rawTracer) {
        return new OtelPropagator(openTelemetrySdk.getPropagators(),
                rawTracer);
    }
}
