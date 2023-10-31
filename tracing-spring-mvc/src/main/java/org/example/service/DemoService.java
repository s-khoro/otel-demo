package org.example.service;

import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DemoService {

    @NewSpan(name = "operation1")
    public void operation1() throws Exception {
        log.info("OP1");
        operation2(Math.random());
    }

    @NewSpan(name = "operation2")
    public void operation2(@SpanTag("operationValue") double value) throws Exception {
        Thread.sleep(300);
        log.info("OP2, value={}", value);
    }
}
