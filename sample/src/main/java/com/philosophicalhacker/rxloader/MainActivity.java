package com.philosophicalhacker.rxloader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.philosophicalhacker.lib.RxLoader;
import com.squareup.leakcanary.LeakCanary;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    interface StoryApiService {
        @GET("topstories.json") Observable<List<Integer>> getStories();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LeakCanary.install(getApplication());
        StoryApiService storyApiService = new Retrofit.Builder()
                .baseUrl("https://hacker-news.firebaseio.com/v0/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(StoryApiService.class);
        storyApiService.getStories()
                .compose(RxLoader.<List<Integer>>from(getSupportLoaderManager(), this))
                .subscribeOn(Schedulers.from(AsyncTask.THREAD_POOL_EXECUTOR))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Integer>>() {
                    @Override
                    public void accept(List<Integer> integers) {
                        Log.d(TAG, "call() called with: integers = [" + integers + "]");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Log.e(TAG, "call() called with: throwable = [" + throwable + "]");
                    }
                });

    }
}
