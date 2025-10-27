package com.llm.core.interceptor.impl;

import com.llm.core.interceptor.Interceptor;
import com.llm.monitoring.Metrics;
import com.llm.provider.ProviderAdapter;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;

/**
 * Collects metrics about requests.
 */
public class MetricsInterceptor implements Interceptor {

    private final Metrics metrics;

    public MetricsInterceptor(Metrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public Response intercept(Request request, Chain chain, ProviderAdapter adapter) {
        metrics.increment("requests.total");
        String provider = adapter.getType().getId();
        metrics.increment("requests." + provider);
        long start = System.nanoTime();
        Response response = chain.proceed(request);
        long duration = System.nanoTime() - start;
        metrics.incrementBy("latency." + provider, duration);
        return response;
    }
}
