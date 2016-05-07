package com.lvack.MasterStats.Api;

import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import com.lvack.MasterStats.Util.GsonProvider;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * RiotApiResponseClass for MasterStats
 *
 * @author Leon Vack
 */

/**
 * class representing a response by the riot api
 * @param <T> type of the response object
 */
public class RiotApiResponse<T> {
    private static final Gson GSON = GsonProvider.getGSON();
    private final Class<T> clazz;
    private final Type type;
    private RateLimiter rateLimiter;
    private Future<Response> responseFuture;
    private AsyncInvokerProvider builder;
    private Response response;

    public RiotApiResponse(AsyncInvokerProvider builder, Type type) {
        this.type = type;
        this.clazz = null;
        response = null;
        this.builder = builder;
    }

    public RiotApiResponse(AsyncInvokerProvider builder, Class<T> clazz) {
        this.type = null;
        this.clazz = clazz;
        this.builder = builder;
    }

    /**
     * acquires read from the rate limiter (if provided) generates a new async invoker,
     * invokes it and stores future response
     */
    public void sendGet() {
        if (rateLimiter != null) rateLimiter.acquire();
        responseFuture = builder.get().get();
    }

    /**
     * reads response from future request if not already set
     */
    private void readResponse() {
        if (response != null) return;
        try {
            response = responseFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * reads the response and if the request was successful returns the object acquired
     * otherwise returns null
     * @return the response object
     */
    public T get() {
        readResponse();
        if (response.getStatus() != 200) return null;
        String json = response.readEntity(String.class);
        return GSON.fromJson(json, type != null ? type : clazz);
    }

    /**
     * retries sending the request to the api and set response to null
     */
    public void resend() {
        responseFuture.cancel(true);
        responseFuture = builder.get().get();
        response = null;
    }

    /**
     * reads the response and returns it
     * @return the response
     */
    public Response getResponse() {
        readResponse();
        return response;
    }
    public void setRateLimiter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    /**
     * lambda interface to generate new async invokers to resend the request
     */
    public interface AsyncInvokerProvider {
        AsyncInvoker get();
    }
}
