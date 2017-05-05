package com.philosophicalhacker.rxloader;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
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

    @GET("askstories.json") Observable<List<Integer>> getAskStories();
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    LeakCanary.install(getApplication());
    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectNetwork()
        .penaltyDeath()
        .build());
    final RxLoader rxLoader = new RxLoader(this, getSupportLoaderManager());
    StoryApiService storyApiService = new Retrofit.Builder()
        .baseUrl("https://hacker-news.firebaseio.com/v0/")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(StoryApiService.class);
    storyApiService
        .getStories()
        .compose(rxLoader.<List<Integer>>makeObservableTransformer(0))
        .subscribe(new Consumer<List<Integer>>() {
          @Override public void accept(List<Integer> integers) {
            ((TextView) findViewById(R.id.helloWorld)).setText(String.valueOf(integers));
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(Throwable throwable) {
            Log.e(TAG, "call() called with: throwable = [" + throwable + "]");
          }
        });
    storyApiService
        .getAskStories()
        .compose(rxLoader.<List<Integer>>makeObservableTransformer(1))
        .subscribe(new Consumer<List<Integer>>() {
          @Override public void accept(List<Integer> integers) {
            Log.d(TAG, "call() called with: integers = [" + integers + "]");
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(Throwable throwable) {
            Log.e(TAG, "call() called with: throwable = [" + throwable + "]");
          }
        });
  }
}
