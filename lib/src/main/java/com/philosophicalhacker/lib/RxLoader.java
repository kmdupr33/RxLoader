package com.philosophicalhacker.lib;

import android.content.Context;
import android.support.v4.app.LoaderManager;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.annotations.NonNull;

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

  public <T> ObservableTransformer<T, T> makeObservableTransformer(final int id,
      final boolean forceReload) {
    return new ObservableTransformer<T, T>() {
      @Override public ObservableSource<T> apply(@NonNull final Observable<T> upstream) {
        return new ReactiveLoaders.LoaderObservable<>(context,
            new LoadRequest<T>(loaderManager, forceReload, id),
            new ReactiveType.Observable<>(upstream));
      }
    };
  }

  public <T> ObservableTransformer<T, T> makeObservableTransformer(final int id) {
    return makeObservableTransformer(id, false);
  }

  public <T> SingleTransformer<T, T> makeSingleTransformer() {
    return makeSingleTransformer(0, false);
  }

  public <T> SingleTransformer<T, T> makeSingleTransformer(int loaderId) {
    return makeSingleTransformer(loaderId, false);
  }

  private <T> SingleTransformer<T, T> makeSingleTransformer(final int loadId,
      final boolean forceReload) {
    return new SingleTransformer<T, T>() {
      @Override public SingleSource<T> apply(@NonNull final Single<T> upstream) {
        return new ReactiveLoaders.LoaderSingle<>(context,
            new LoadRequest<T>(loaderManager, forceReload, loadId),
            new ReactiveType.Single<>(upstream));
      }
    };
  }
}
