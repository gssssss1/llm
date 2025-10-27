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
        String provider = adapter.getType().getId();
        metrics.increment("requests.total");
        metrics.increment("requests.provider." + provider);

        long start = System.nanoTime();
        try {
            Response response = chain.proceed(request);
            long duration = System.nanoTime() - start;
            metrics.recordLatency(provider, duration);
            if (response.getUsage() != null) {
                metrics.recordTokens(provider, response.getUsage());
            }
            metrics.increment("requests.success");
            return response;
        } catch (Exception ex) {
            metrics.increment("requests.failure");
            metrics.increment("requests.failure." + provider);
            throw ex;
        } finally {
            metrics.logRequest(provider, System.nanoTime() - start);
        }
    }
}
