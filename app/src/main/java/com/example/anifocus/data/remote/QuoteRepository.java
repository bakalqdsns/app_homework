package com.example.anifocus.data.remote;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class QuoteRepository {
    private final OkHttpClient client;
    private final Gson gson;
    private final Handler mainHandler;

    public interface QuoteCallback {
        void onSuccess(List<Quote> quotes);
        void onError(String error);
    }

    public static class Quote {
        public String text;
        public String anime;
        public String character;

        public Quote(String text, String anime, String character) {
            this.text = text;
            this.anime = anime;
            this.character = character;
        }
    }

    public QuoteRepository() {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        gson = new Gson();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    private static final String HITOKOTO_API = "https://v1.hitokoto.cn/?c=b&encode=json";

    public void fetchQuotes(QuoteCallback callback) {
        Request request = new Request.Builder()
                .url(HITOKOTO_API)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String body = response.body().string();
                    try {
                        JsonObject json = gson.fromJson(body, JsonObject.class);
                        Quote quote = new Quote(
                                json.get("hitokoto") != null ? json.get("hitokoto").getAsString() : "",
                                json.get("from") != null ? json.get("from").getAsString() : "",
                                json.get("from_who") != null ? json.get("from_who").getAsString() : ""
                        );
                        List<Quote> result = new ArrayList<>();
                        result.add(quote);
                        mainHandler.post(() -> callback.onSuccess(result));
                    } catch (Exception e) {
                        mainHandler.post(() -> callback.onError("JSON parse error"));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Network error: " + response.code()));
                }
            }
        });
    }

    public void fetchFromNetwork(String url, QuoteCallback callback) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String body = response.body().string();
                    try {
                        JsonObject json = gson.fromJson(body, JsonObject.class);
                        Quote quote = new Quote(
                                json.get("text") != null ? json.get("text").getAsString() : "",
                                json.get("anime") != null ? json.get("anime").getAsString() : "",
                                json.get("character") != null ? json.get("character").getAsString() : ""
                        );
                        List<Quote> result = new ArrayList<>();
                        result.add(quote);
                        mainHandler.post(() -> callback.onSuccess(result));
                    } catch (Exception e) {
                        mainHandler.post(() -> callback.onError("JSON parse error"));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Network error: " + response.code()));
                }
            }
        });
    }
}
