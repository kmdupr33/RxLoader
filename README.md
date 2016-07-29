[ ![Download](https://api.bintray.com/packages/kmdupr33/RxLoader/com.philosophicalhacker.rxloader%3Arxloader/images/download.svg) ](https://bintray.com/kmdupr33/RxLoader/com.philosophicalhacker.rxloader%3Arxloader/_latestVersion)

# RxLoader

`compose()` your `Observable`s into ones that load data that's cached across activity orientation changes without leaking your `Activity` and without you having to worry about unsubscribing

RxLoader accomplishes this by building on top of [Android Loaders](https://developer.android.com/guide/components/loaders.html), so the library is super lightweight (currently less than 150 LOC)

There's a lot of data loading solutions already for Android. I detail why I felt we needed another one [here](http://www.philosophicalhacker.com/post/rxloader-boilerplate-free-data-loading-with-loaders-and-rxjava)

`compile 'com.philosophicalhacker.rxloader:rxloader:0.1.0'`

## Basic Usage

```java
interface StoryApiService {
    Observable<List<Integer>> getStories();
}

//...

@Override
protected void onCreate(Bundle savedInstanceState) {
    storyApiService.getStories()
        .compose(RxLoader.<List<Integer>>from(this))
        .subscribeOn(Schedulers.from(AsyncTask.THREAD_POOL_EXECUTOR))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<List<Integer>>() {
            @Override
            public void call(List<Integer> integers) {
                Log.d(TAG, "call() called with: integers = [" + integers + "]");
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.e(TAG, "call() called with: throwable = [" + throwable + "]");
            }
        });
}
```

```
Copyright 2016 Kevin Matthew Dupree

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
```
