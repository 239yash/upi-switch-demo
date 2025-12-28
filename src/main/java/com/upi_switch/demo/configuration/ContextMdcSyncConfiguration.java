package com.upi_switch.demo.configuration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

@Configuration
public class ContextMdcSyncConfiguration {

    private static final String TRACE_ID = "traceId";

    @PostConstruct
    void contextOperatorHook() {
        Hooks.onEachOperator(
                TRACE_ID, Operators.lift((sc, subscriber) -> new MdcContextLifter<>(subscriber)));
    }

    @PreDestroy
    void cleanupHook() {
        Hooks.resetOnEachOperator(TRACE_ID);
    }

    static class MdcContextLifter<T> implements CoreSubscriber<T> {

        private final CoreSubscriber<T> coreSubscriber;

        public MdcContextLifter(CoreSubscriber<T> coreSubscriber) {
            this.coreSubscriber = coreSubscriber;
        }

        @Override
        public void onSubscribe(Subscription s) {
            this.copyToMdc(coreSubscriber.currentContext());
            coreSubscriber.onSubscribe(s);
        }

        @Override
        public void onNext(T t) {
            this.copyToMdc(coreSubscriber.currentContext());
            coreSubscriber.onNext(t);
        }

        @Override
        public void onError(Throwable throwable) {
            this.copyToMdc(coreSubscriber.currentContext());
            coreSubscriber.onError(throwable);
        }

        @Override
        public void onComplete() {
            this.copyToMdc(coreSubscriber.currentContext());
            coreSubscriber.onComplete();
        }

        @Override
        public Context currentContext() {
            return coreSubscriber.currentContext();
        }

        private void copyToMdc(Context context) {
            if (context.hasKey(TRACE_ID)) {
                MDC.put(TRACE_ID, context.get(TRACE_ID));
            } else {
                MDC.remove(TRACE_ID);
            }
        }
    }
}
