package org.example.service;

import io.micrometer.tracing.Tracer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Slf4j
@Service
public class DemoService {

    private final Tracer tracer;

    public void operation1() throws Exception {
        try (Tracer.SpanInScope operation1 = tracer.withSpan(tracer.spanBuilder()
                .name("operation-1-span").start())) {
            log.info("OP1");
            operation2(Math.random());
        }
    }

    public void operation2(double value) throws Exception {
        try (Tracer.SpanInScope operation2 = tracer.withSpan(tracer.spanBuilder()
                .name("operation-2-span")
                .tag("op2arg", value).start())) {
            Thread.sleep(300);
            log.info("OP2, value={}", value);
        }
    }
}
