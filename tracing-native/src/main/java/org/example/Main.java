package org.example;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import lombok.extern.slf4j.Slf4j;
import org.example.config.OtelNativeConfiguration;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Main {


    public Main() {
        super();
    }

    public static void main(String[] args) throws Exception {
        OpenTelemetrySdk openTelemetrySdk = OtelNativeConfiguration.buildSdk();
        io.opentelemetry.api.trace.Tracer rawTracer = OtelNativeConfiguration.buildRawTracer(openTelemetrySdk);
        Propagator propagator = OtelNativeConfiguration.propagator(openTelemetrySdk, rawTracer);
        Tracer tracer = OtelNativeConfiguration.buildTracer(rawTracer);

        // sdk must be gracefully stopped
        try (openTelemetrySdk) {
            Span nativeSpan = tracer.nextSpan().name("nativeSpan");

            try (Tracer.SpanInScope spanInScope = tracer.withSpan(nativeSpan.start())) {
                nativeSpan.tag("nativeTag", "nativeTagValue");

                log.info("Starting..");
                Thread.sleep(200);
                log.info("Completed");

                // context propagation
                Map<String, String> map = new HashMap<>();
                propagator.inject(tracer.currentTraceContext().context(),
                        map, new Propagator.Setter<Map<String, String>>() {
                            @Override
                            public void set(Map<String, String> stringStringMap, String s, String s1) {
                                stringStringMap.put(s, s1);
                            }
                        });

                log.info("Traceparent header value: {}", map.get("traceparent"));

            } finally {
                nativeSpan.end();
            }
        }
    }
}