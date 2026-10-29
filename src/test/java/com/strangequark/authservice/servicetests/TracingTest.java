package com.strangequark.authservice.servicetests;

import com.strangequark.authservice.utility.TraceUtility;
import io.micrometer.tracing.Span;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureObservability
@AutoConfigureMockMvc
public class TracingTest extends BaseServiceTest {
    @Autowired
    private Tracer tracer;
    @Autowired
    private ObservationRegistry observationRegistry;
    @Autowired
    private TraceUtility traceUtility;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    @Qualifier("telemetryExecutor")
    private Executor executor;

    @Test
    void incomingTraceIsReturnedAndRequestScopeIsClearedTest() throws Exception {
        String traceId = "4bf92f3577b34da6a3ce929d0e0e4736";
        mockMvc.perform(get("/api/auth/health")
                        .header("traceparent", "00-" + traceId + "-00f067aa0ba902b7-01"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Trace-ID", traceId));

        assertNull(tracer.currentSpan());
        assertNull(MDC.get("traceId"));
    }

    @Test
    void httpAsyncAndKafkaContinueTheSameTraceTest() throws Exception {
        ClientHttpRequestFactory originalFactory = restTemplate.getRequestFactory();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        Observation observation = Observation.start("tracing-test", observationRegistry);
        String traceId;
        try(Observation.Scope ignored = observation.openScope()) {
            traceId = tracer.currentSpan().context().traceId();
        }
        CompletableFuture<String> asynchronousTrace = new CompletableFuture<>();

        try {
            server.expect(requestTo("http://example.test/trace"))
                    .andExpect(header("traceparent", startsWith("00-" + traceId + "-")))
                    .andRespond(withSuccess());

            try(Observation.Scope ignored = observation.openScope()) {
                restTemplate.getForObject("http://example.test/trace", String.class);
                executor.execute(() -> {
                    try {
                        ProducerRecord<String, String> record = new ProducerRecord<>("trace-test", "event");
                        traceUtility.addTraceHeaders(record);
                        String traceparent = new String(record.headers().lastHeader("traceparent").value(),
                                StandardCharsets.UTF_8);
                        assertEquals(traceId, MDC.get("traceId"));
                        asynchronousTrace.complete(traceparent);
                    } catch(Throwable error) {
                        asynchronousTrace.completeExceptionally(error);
                    }
                });
            }

            assertTrue(asynchronousTrace.get(5, TimeUnit.SECONDS).startsWith("00-" + traceId + "-"));
            CompletableFuture<String> nextTaskTrace = new CompletableFuture<>();
            executor.execute(() -> nextTaskTrace.complete(MDC.get("traceId")));
            assertNull(nextTaskTrace.get(5, TimeUnit.SECONDS));
            server.verify();
        } finally {
            observation.stop();
            restTemplate.setRequestFactory(originalFactory);
        }
    }

    @Test
    void kafkaCallbackRestoresTraceAndCleansUpAfterFailureTest() {
        Span span = tracer.nextSpan().name("callback-test").start();
        Callback callback;
        try(Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            callback = traceUtility.wrapCallback((metadata, exception) -> {
                assertEquals(span.context().traceId(), MDC.get("traceId"));
                throw new IllegalStateException("callback failed");
            });
        } finally {
            span.end();
        }

        assertThrows(IllegalStateException.class, () -> callback.onCompletion(null, null));
        assertNull(tracer.currentSpan());
        assertNull(MDC.get("traceId"));
    }
}
