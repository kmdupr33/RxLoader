package com.philosophicalhacker.lib;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

public class RxLoader {

  private final Context context;
  private final LoaderManager loaderManager;

  public RxLoader(Context context, LoaderManager loaderManager) {
    this.context = context;
    this.loaderManager = loaderManager;
  }

  public <T> ObservableTransformer<T, T> makeObservableTransformer() {
    return makeObservableTransformer(0, false);
  }

  public <T> ObservableTransformer<T, T> makeObservableTransformer(final int id, final boolean forceReload) {
    return new ObservableTransformer<T, T>() {
      @Override public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
        return Observable.create(
            new LoaderCallbackAsyncEmitter<>(upstream, context,
                loaderManager, id,
                forceReload));
      }
    };
  }

  public <T> ObservableTransformer<T, T> makeObservableTransformer(final int id) {
    return makeObservableTransformer(id, false);
  }

  private static class ObservableLoader<T> extends Loader<T> {

    private final Observable<T> upstreamObservable;
    private Throwable error;

    ObservableLoader(Context context, Observable<T> upstreamObservable) {
      super(context);
      this.upstreamObservable = upstreamObservable;
    }

    @Override protected void onStartLoading() {
      super.onStartLoading();
      forceLoad();
    }

    @Override protected void onForceLoad() {
      super.onForceLoad();
      upstreamObservable.subscribe(new Consumer<T>() {
        @Override public void accept(@NonNull T t) throws Exception {
          deliverResult(t);
        }
      }, new Consumer<Throwable>() {
        @Override public void accept(@NonNull Throwable throwable) throws Exception {
          error = throwable;
          deliverResult(null);
        }
      });
    }
  }

  private static class LoaderCallbackAsyncEmitter<T> implements ObservableOnSubscribe<T> {
    private final Observable<T> upstreamObservable;
    private final Context context;
    private final LoaderManager loaderManager;
    private final int id;
    private final boolean forceReload;

    LoaderCallbackAsyncEmitter(Observable<T> tObservable, Context context,
        LoaderManager loaderManager, int id, boolean forceReload) {
      upstreamObservable = tObservable;
      this.context = context;
      this.loaderManager = loaderManager;
      this.id = id;
      this.forceReload = forceReload;
    }

    @Override public void subscribe(@NonNull final ObservableEmitter<T> e) throws Exception {
      final Loader<T> tLoader =
          loaderManager.initLoader(id, null, new LoaderManager.LoaderCallbacks<T>() {
            @Override public Loader<T> onCreateLoader(int id, Bundle args) {
              return new ObservableLoader<>(context, upstreamObservable);
            }

            @Override public void onLoadFinished(Loader<T> loader, T data) {
              final Throwable error = ((ObservableLoader) loader).error;
              if (error != null) {
                e.onError(error);
              } else {
                e.onNext(data);
                e.onComplete();
              }
            }

            @Override public void onLoaderReset(Loader<T> loader) {
            }
          });
      if (forceReload) {
        tLoader.forceLoad();
      }
    }
  }
}
