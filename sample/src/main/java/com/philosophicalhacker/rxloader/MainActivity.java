package com.philosophicalhacker.rxloader;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.philosophicalhacker.lib.RxLoader;
import com.squareup.leakcanary.LeakCanary;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import java.util.List;
import java.util.concurrent.TimeUnit;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    LeakCanary.install(getApplication());
    // We crash if we're doing work on the main thread to make sure we're using schedulers correctly
    StrictMode.setThreadPolicy(
        new StrictMode.ThreadPolicy.Builder().detectNetwork().penaltyDeath().build());
    final RxLoader rxLoader = new RxLoader(this, getSupportLoaderManager());
    StoryApiService storyApiService = makeStoryApiService();
    storyApiService.getStories()
                   // We deliver the data over time to make sure that we're handling stopped loaders properly
                   .flatMap(new Function<List<Integer>, ObservableSource<Integer>>() {
                     @Override
                     public ObservableSource<Integer> apply(@NonNull List<Integer> integers)
                         throws Exception {
                       return Observable
                           .fromIterable(integers)
                           .zipWith(Observable.interval(1, TimeUnit.SECONDS),
                               new BiFunction<Integer, Long, Integer>() {
                                 @Override
                                 public Integer apply(@NonNull Integer integer, @NonNull Long aLong)
                                     throws Exception {
                                   return integer;
                                 }
                               });
                     }
                   })
                   .compose(rxLoader.<Integer>makeObservableTransformer(0))
                   .subscribe(new Consumer<Integer>() {
                     @Override public void accept(Integer integer) {
                       ((TextView) findViewById(R.id.helloWorld)).setText(String.valueOf(integer));
                     }
                   }, new Consumer<Throwable>() {
                     @Override public void accept(Throwable throwable) {
                       Log.wtf(TAG, throwable);
                     }
                   });
    // We make two load requests simultaneously to ensure that we don't get an illegal state exception
    storyApiService
        .getAskStories()
        .compose(rxLoader.<List<Integer>>makeSingleTransformer(1))
        .subscribe(new Consumer<List<Integer>>() {
          @Override public void accept(List<Integer> integers) {
            Log.d(TAG, "call() called with: integers = [" + integers + "]");
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(Throwable throwable) {
            Log.e(TAG, "call() called with: throwable = [" + throwable + "]");
          }
        });
    // We launch a second activity to make sure that we're handling stopped loaders properly
    findViewById(R.id.launchDetailButton).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        final Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        startActivity(intent);
      }
    });
  }

  //------------------------------------------------------------------
  // Helpers
  //------------------------------------------------------------------
  private StoryApiService makeStoryApiService() {
    return new Retrofit.Builder()
        .baseUrl("https://hacker-news.firebaseio.com/v0/")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(StoryApiService.class);
  }
}
