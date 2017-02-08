package com.philosophicalhacker.lib;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import rx.Emitter;
import rx.Observable;
import rx.Observer;
import rx.functions.Action1;

/**
 * Created by mattdupree on 7/23/16.
 */

public class RxLoader {

    public static <T> Observable.Transformer<T, T> from(final AppCompatActivity activity) {
        return from(activity, 0, false);
    }

    public static <T> Observable.Transformer<T, T> from(final AppCompatActivity activity,
                                                        final int id, final boolean forceReload) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> tObservable) {
                return Observable.fromEmitter(
		                new LoaderCallbackAsyncEmitter<>(tObservable, activity,
                                activity.getSupportLoaderManager(),
                                id, forceReload), Emitter.BackpressureMode.DROP);
            }
        };
    }

    public static <T> Observable.Transformer<T, T> from(final AppCompatActivity activity,
                                                        final int id) {
        return from(activity, id, false);
    }

    private static class ObservableLoader<T> extends Loader<T> {

        private final Observable<T> mTObservable;
        private Throwable mError;

        ObservableLoader(Context context, Observable<T> tObservable) {
            super(context);
            mTObservable = tObservable;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        @Override
        protected void onForceLoad() {
            super.onForceLoad();
            mTObservable.subscribe(new Observer<T>() {
                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable e) {
                    mError = e;
                    deliverResult(null);
                }

                @Override
                public void onNext(T t) {
                    deliverResult(t);
                }
            });
        }
    }

    private static class LoaderCallbackAsyncEmitter<T> implements Action1<Emitter<T>> {
        private final Observable<T> mTObservable;
        private final Context mContext;
        private final LoaderManager mLoaderManager;
        private final int mId;
        private final boolean mForceReload;

        LoaderCallbackAsyncEmitter(Observable<T> tObservable, Context context,
                                   LoaderManager loaderManager,
                                   int id,
                                   boolean forceReload) {
            mTObservable = tObservable;
            mContext = context;
            mLoaderManager = loaderManager;
            mId = id;
            mForceReload = forceReload;
        }

@Override
public void call(final Emitter<T> emitter) {
    synchronized( mLoaderManager )
    {
        final Loader<T> tLoader
		        = mLoaderManager.initLoader( mId, null,
		                                     new LoaderManager.LoaderCallbacks<T>()
		                                     {
			                                     @Override
			                                     public Loader<T> onCreateLoader(
					                                                                    int id,
					                                                                    Bundle args )
			                                     {
				                                     return new ObservableLoader<>(
						                                                                  mContext,
						                                                                  mTObservable );
			                                     }

			                                     @Override
			                                     public void onLoadFinished(
					                                                               Loader<T> loader,
					                                                               T data )
			                                     {
				                                     final Throwable error = ((ObservableLoader) loader).mError;
				                                     if( error != null )
				                                     {
					                                     emitter.onError(
							                                     error );
				                                     }
				                                     else
				                                     {
					                                     emitter.onNext( data );
					                                     emitter.onCompleted();
				                                     }
			                                     }

			                                     @Override
			                                     public void onLoaderReset(
					                                                              Loader<T> loader )
			                                     {
			                                     }
		                                     } );
        if (mForceReload) {
	        tLoader.forceLoad();
        }
    }
}
    }
}
