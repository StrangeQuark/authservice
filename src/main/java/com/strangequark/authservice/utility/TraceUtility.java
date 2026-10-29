package com.strangequark.authservice.utility;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class TraceUtility {
    private final Tracer tracer;
    private final Propagator propagator;

    public TraceUtility(Tracer tracer, Propagator propagator) {
        this.tracer = tracer;
        this.propagator = propagator;
    }

    public void addTraceHeaders(ProducerRecord<?, ?> record) {
        Span span = tracer.currentSpan();

        if(span == null)
            return;

        propagator.inject(span.context(), record, (carrier, key, value) -> {
            carrier.headers().remove(key);
            carrier.headers().add(key, value.getBytes(StandardCharsets.UTF_8));
        });
    }

    public Callback wrapCallback(Callback callback) {
        Span span = tracer.currentSpan();

        if(span == null)
            return callback;

        return (recordMetadata, exception) -> {
            try(Tracer.SpanInScope ignored = tracer.withSpan(span)) {
                callback.onCompletion(recordMetadata, exception);
            }
        };
    }
}
