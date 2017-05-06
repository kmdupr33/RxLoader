package com.philosophicalhacker.lib;

import io.reactivex.Scheduler;
import io.reactivex.functions.Consumer;

/**
 * Abstracts over different reactive types (e.g., {@link Single}, {@link Observable}), so
 * {@link RxLoaderCallbacks.RxAndroidLoader} can have a common interface.
 *
 * @param <T>
 */
interface ReactiveType<T> {
  ReactiveType<T> subscribeOn(Scheduler scheduler);
  void subscribe(Consumer<T> consumer, Consumer<Throwable> throwableConsumer);

  ReactiveType<T> observeOn(Scheduler scheduler);

  class Single<T> implements ReactiveType<T> {
    private io.reactivex.Single<T> upstream;

    Single(io.reactivex.Single<T> upstream) {
      this.upstream = upstream;
    }

    @Override public ReactiveType<T> subscribeOn(Scheduler scheduler) {
      upstream = upstream.subscribeOn(scheduler);
      return this;
    }

    @Override public void subscribe(Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
      upstream.subscribe(consumer, throwableConsumer);
    }

    @Override public ReactiveType<T> observeOn(Scheduler scheduler) {
      upstream = upstream.observeOn(scheduler);
      return this;
    }
  }

  class Observable<T> implements ReactiveType<T> {
    private io.reactivex.Observable<T> upstream;

    Observable(io.reactivex.Observable<T> upstream) {
      this.upstream = upstream;
    }

    @Override public ReactiveType<T> subscribeOn(Scheduler scheduler) {
      upstream = upstream.subscribeOn(scheduler);
      return this;
    }

    @Override public void subscribe(Consumer<T> consumer, Consumer<Throwable> throwableConsumer) {
      upstream.subscribe(consumer, throwableConsumer);
    }

    @Override public ReactiveType<T> observeOn(Scheduler scheduler) {
      upstream = upstream.observeOn(scheduler);
      return this;
    }
  }
}
