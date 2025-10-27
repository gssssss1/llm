package com.llm.core.interceptor;

import com.llm.provider.ProviderAdapter;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;

/**
 * Interceptor allowing pre/post processing around provider calls.
 */
public interface Interceptor {

    Response intercept(Request request, Chain chain, ProviderAdapter adapter);

    interface Chain {
        Response proceed(Request request);
    }
}
