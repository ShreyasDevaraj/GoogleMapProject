package com.example.sample.googlemapproject.service;

import android.support.v4.util.LruCache;
import android.util.Log;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import com.example.sample.googlemapproject.model.Crime;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;


public class ChicagoCrimeNetworkService {

    private static final String TAG = ChicagoCrimeNetworkService.class.getSimpleName();
    private static final String baseUrl = "http://api1.chicagopolice.org/clearpath/api/1.0/";
    private final NetworkAPI networkAPI;
    private final LruCache<Class<?>, Observable<?>> apiObservables;

    public ChicagoCrimeNetworkService() {
        this(baseUrl);
    }

    public ChicagoCrimeNetworkService(final String baseUrl) {
        Log.d(TAG, "Creating service ");
        final OkHttpClient okHttpClient = buildClient();
        apiObservables = new LruCache<>(10);
        final Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).addCallAdapterFactory(RxJava2CallAdapterFactory.create()).client(okHttpClient).build();
        networkAPI = retrofit.create(NetworkAPI.class);
    }

    public NetworkAPI getAPI() {
        return networkAPI;
    }

    private OkHttpClient buildClient() {

        final OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(final Chain chain) throws IOException {
                final Response response = chain.proceed(chain.request());
                // Can do anything with response here if we ant to grab a specific cookie or something..
                return response;
            }
        });

        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(final Chain chain) throws IOException {
                //We can add whatever we want to our request headers.
                final Request request = chain.request().newBuilder().addHeader("Accept", "application/json").build();
                return chain.proceed(request);
            }
        });

        return builder.connectTimeout(100, TimeUnit.SECONDS).readTimeout(100, TimeUnit.SECONDS).build();
    }

    public void clearCache() {
        apiObservables.evictAll();
    }

    public Observable<?> getPreparedObservable(Observable<?> unPreparedObservable, Class<?> clazz, boolean cacheObservable, boolean useCache) {

        Observable<?> preparedObservable = null;

        if(useCache)//this way we don't reset anything in the cache if this is the only instance of us not wanting to use it.
        {
            preparedObservable = apiObservables.get(clazz);
        }

        if(preparedObservable != null) {
            return preparedObservable;
        }


        //we are here because we have never created this observable before or we didn't want to use the cache...

        preparedObservable = unPreparedObservable.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());

        if(cacheObservable) {
            preparedObservable = preparedObservable.cache();
            apiObservables.put(clazz, preparedObservable);
        }
        return preparedObservable;
    }

    /**
     * Interface API which provides the REST call request definition
     */
    public interface NetworkAPI {


        @GET("crimes/major")
        Observable<Crime[]> getMajorCrimeList(@Query("dateOccurredStart") String dateOccurredStart, @Query("max") String maxResults, @Query("offset") String offset);

        @GET("crimes/type")
        Observable<Crime[]> getCrimesByType(@Query("primary") String crimeType, @Query("dateOccurredStart") String dateOccurredStart, @Query("max") String maxResults, @Query("offset") String offset);

    }
}
