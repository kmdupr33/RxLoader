package com.philosophicalhacker.lib;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

class RxLoaderCallbacks<T> implements LoaderManager.LoaderCallbacks<T> {

  private final Context context;
  private final ReactiveType<T> reactiveType;
  private final ReactiveEmitter<T> reactiveEmitter;

  RxLoaderCallbacks(Context context, ReactiveType<T> reactiveType, ReactiveEmitter<T> reactiveEmitter) {
    this.context = context;
    this.reactiveType = reactiveType;
    this.reactiveEmitter = reactiveEmitter;
  }

  @Override public Loader<T> onCreateLoader(int id, Bundle args) {
    return new RxAndroidLoader<>(context, reactiveType);
  }

  @Override public void onLoadFinished(Loader<T> loader, T data) {
    final Throwable error = ((RxAndroidLoader) loader).getError();
    if (error != null) {
      reactiveEmitter.onError(error);
    } else {
      reactiveEmitter.onLoadFinished(data);
    }
  }

  @Override public void onLoaderReset(Loader<T> loader) {}

  //------------------------------------------------------------------
  // RxAndroidLoader
  //------------------------------------------------------------------
  private static class RxAndroidLoader<T> extends Loader<T> {
    private final ReactiveType<T> reactiveType;
    private static final String TAG = RxAndroidLoader.class.getSimpleName();
    private T pendingData;

    private Throwable error;

    RxAndroidLoader(Context context, ReactiveType<T> reactiveType) {
      super(context);
      this.reactiveType = reactiveType;
    }

    @Override protected void onStartLoading() {
      super.onStartLoading();
      Log.d(TAG, "onStartLoading() called");
      if (pendingData != null) {
        Log.d(TAG, "delivering pending data");
        deliverResult(pendingData);
        pendingData = null;
      } else {
        forceLoad();
      }
    }

    @Override protected void onForceLoad() {
      super.onForceLoad();
      Log.d(TAG, "onForceLoad() called");
      reactiveType
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new Consumer<T>() {
            @Override public void accept(@NonNull T t) throws Exception {
              safeDeliverResult(t);
            }
          }, new Consumer<Throwable>() {
            @Override public void accept(@NonNull Throwable throwable) throws Exception {
              error = throwable;
              safeDeliverResult(null);
            }
          });
    }

    Throwable getError() {
      return error;
    }

    //------------------------------------------------------------------
    // Helpers
    //------------------------------------------------------------------
    private void safeDeliverResult(T t) {
      if (isStarted()) {
        Log.d(TAG, "delivering result");
        deliverResult(t);
      } else {
        Log.d(TAG, "storing result");
        pendingData = t;
      }
    }
  }
}
