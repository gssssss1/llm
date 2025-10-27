package com.llm.core.interceptor;

import com.llm.provider.ProviderAdapter;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Chain of interceptors that execute in order.
 */
public class InterceptorChain {

    private final List<Interceptor> interceptors;

    private InterceptorChain(List<Interceptor> interceptors) {
        this.interceptors = new ArrayList<>(interceptors);
    }

    public Response intercept(Request request, ProviderAdapter adapter) {
        return new RealChain(interceptors, 0, request, adapter).proceed(request);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<Interceptor> interceptors = new ArrayList<>();

        public Builder addInterceptor(Interceptor interceptor) {
            interceptors.add(interceptor);
            return this;
        }

        public InterceptorChain build() {
            return new InterceptorChain(interceptors);
        }
    }

    private static class RealChain implements Interceptor.Chain {
        private final List<Interceptor> interceptors;
        private final int index;
        private final Request request;
        private final ProviderAdapter adapter;

        public RealChain(List<Interceptor> interceptors, int index, Request request, ProviderAdapter adapter) {
            this.interceptors = interceptors;
            this.index = index;
            this.request = request;
            this.adapter = adapter;
        }

        @Override
        public Response proceed(Request request) {
            if (index >= interceptors.size()) {
                return adapter.send(request);
            }

            Interceptor next = interceptors.get(index);
            RealChain nextChain = new RealChain(interceptors, index + 1, request, adapter);
            return next.intercept(request, nextChain, adapter);
        }
    }
}
