package com.philosophicalhacker.lib;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

class LoadRequest<T> {
  private final LoaderManager loaderManager;
  private final boolean forceReload;
  private final int id;

  LoadRequest(LoaderManager loaderManager, boolean forceReload, int id) {
    this.loaderManager = loaderManager;
    this.forceReload = forceReload;
    this.id = id;
  }

  void execute(RxLoaderCallbacks<T> callbacks) {
    final Loader<T> tLoader =
        loaderManager.initLoader(id, null, callbacks);
    if (forceReload) {
      tLoader.forceLoad();
    }
  }
}
