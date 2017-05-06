package com.philosophicalhacker.lib;

import io.reactivex.ObservableEmitter;
import io.reactivex.SingleEmitter;

/**
 * Abstracts over different reactive type emitters (e.g., {@link ObservableEmitter}, {@link SingleEmitter},
 * so {@link RxLoaderCallbacks.RxAndroidLoader} can have a common interface.
 * @param <T>
 */
interface ReactiveEmitter<T> {
  void onError(Throwable error);

  void onLoadFinished(T data);
}
