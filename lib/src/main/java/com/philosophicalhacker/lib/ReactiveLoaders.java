package com.philosophicalhacker.lib;

import android.content.Context;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.annotations.NonNull;

class ReactiveLoaders {
  static class LoaderObservable<T> extends Observable<T> {

    private final Context context;
    private final LoadRequest<T> loadRequest;
    private final ReactiveType<T> reactiveType;

    LoaderObservable(Context context, LoadRequest<T> loadRequest, ReactiveType<T> reactiveType) {
      this.context = context;
      this.loadRequest = loadRequest;
      this.reactiveType = reactiveType;
    }

    @Override protected void subscribeActual(final Observer<? super T> observer) {
      loadRequest.execute(new RxLoaderCallbacks<>(context, reactiveType, new ReactiveEmitter<T>() {
        @Override public void onError(Throwable error) {
          observer.onError(error);
        }

        @Override public void onLoadFinished(T data) {
          observer.onNext(data);
        }
      }));
    }
  }

  static class LoaderSingle<T> extends Single<T> {
    private final LoadRequest<T> loadRequest;
    private final Context context;
    private final ReactiveType<T> reactiveType;

    LoaderSingle(Context context, LoadRequest<T> loadRequest, ReactiveType<T> reactiveType) {
      this.loadRequest = loadRequest;
      this.context = context;
      this.reactiveType = reactiveType;
    }

    @Override protected void subscribeActual(@NonNull final SingleObserver<? super T> observer) {
      loadRequest.execute(new RxLoaderCallbacks<>(context, reactiveType, new ReactiveEmitter<T>() {
        @Override public void onError(Throwable error) {
          observer.onError(error);
        }

        @Override public void onLoadFinished(T data) {
          observer.onSuccess(data);
        }
      }));
    }
  }
}
