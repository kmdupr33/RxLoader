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

  public static <T> ObservableTransformer<T, T> from(LoaderManager loaderManager,
      final Context context) {
    return from(loaderManager, context, 0, false);
  }

  public static <T> ObservableTransformer<T, T> from(final LoaderManager loaderManager,
      final Context context, final int id, final boolean forceReload) {
    return new ObservableTransformer<T, T>() {
      @Override public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
        return Observable.create(
            new LoaderCallbackAsyncEmitter<>(upstream, context,
                loaderManager, id,
                forceReload));
      }
    };
  }

  public static <T> ObservableTransformer<T, T> from(final LoaderManager loaderManager,
      Context context, final int id) {
    return from(loaderManager, context, id, false);
  }

  private static class ObservableLoader<T> extends Loader<T> {

    private final Observable<T> mTObservable;
    private Throwable mError;

    ObservableLoader(Context context, Observable<T> tObservable) {
      super(context);
      mTObservable = tObservable;
    }

    @Override protected void onStartLoading() {
      super.onStartLoading();
      forceLoad();
    }

    @Override protected void onForceLoad() {
      super.onForceLoad();
      mTObservable.subscribe(new Consumer<T>() {
        @Override public void accept(@NonNull T t) throws Exception {
          deliverResult(t);
        }
      }, new Consumer<Throwable>() {
        @Override public void accept(@NonNull Throwable throwable) throws Exception {
          mError = throwable;
          deliverResult(null);
        }
      });
    }
  }

  private static class LoaderCallbackAsyncEmitter<T> implements ObservableOnSubscribe<T> {
    private final Observable<T> mTObservable;
    private final Context mContext;
    private final LoaderManager mLoaderManager;
    private final int mId;
    private final boolean mForceReload;

    LoaderCallbackAsyncEmitter(Observable<T> tObservable, Context context,
        LoaderManager loaderManager, int id, boolean forceReload) {
      mTObservable = tObservable;
      mContext = context;
      mLoaderManager = loaderManager;
      mId = id;
      mForceReload = forceReload;
    }

    @Override public void subscribe(@NonNull final ObservableEmitter<T> e) throws Exception {
      final Loader<T> tLoader =
          mLoaderManager.initLoader(mId, null, new LoaderManager.LoaderCallbacks<T>() {
            @Override public Loader<T> onCreateLoader(int id, Bundle args) {
              return new ObservableLoader<>(mContext, mTObservable);
            }

            @Override public void onLoadFinished(Loader<T> loader, T data) {
              final Throwable error = ((ObservableLoader) loader).mError;
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
      if (mForceReload) {
        tLoader.forceLoad();
      }
    }
  }
}
