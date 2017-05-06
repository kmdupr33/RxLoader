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

  /**
   * @param <T> type of data being loaded
   * @return an ObservableTransformer that uses a Loader to emit the items from the upstream
   * Observable, won't force a reload when called again, and uses 0 as its loaderId
   */
  public <T> ObservableTransformer<T, T> makeObservableTransformer() {
    return makeObservableTransformer(0, false);
  }

  /**
   * @param loaderId the Loader's id used by transformed Observable
   * @param forceReload whether to force a reload upon subsequent calls
   * @param <T> type of data being loaded
   * @return an ObservableTransformer that uses a Loader to emit the items from the upstream
   * Observable
   */
  public <T> ObservableTransformer<T, T> makeObservableTransformer(final int loaderId,
      final boolean forceReload) {
    return new ObservableTransformer<T, T>() {
      @Override public ObservableSource<T> apply(@NonNull final Observable<T> upstream) {
        return new ReactiveLoaders.LoaderObservable<>(context,
            new LoadRequest<T>(loaderManager, forceReload, loaderId),
            new ReactiveType.Observable<>(upstream));
      }
    };
  }

  /**
   * @param loaderId the Loader's id used by transformed Observable
   * @param <T> type of data being loaded
   * @return an ObservableTransformer that uses a Loader to emit the items from the upstream
   * Observable and won't force a reload when called again
   */
  public <T> ObservableTransformer<T, T> makeObservableTransformer(final int loaderId) {
    return makeObservableTransformer(loaderId, false);
  }

  /**
   * @param <T> type of data being loaded
   * @return a SingleTransformer that uses a Loader to emit the items from the upstream
   * Single, won't force a reload when called again, and uses 0 as its loaderId
   */
  public <T> SingleTransformer<T, T> makeSingleTransformer() {
    return makeSingleTransformer(0, false);
  }

  /**
   *
   * @param loaderId id the Loader's id used by transformed Single
   * @param <T> type of data being loaded
   * @return a SingleTransformer that uses a Loader to emit the items from the upstream
   * Single and won't force a reload when called again
   */
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
