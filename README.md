[ ![Download](https://api.bintray.com/packages/kmdupr33/RxLoader/com.philosophicalhacker.rxloader%3Arxloader/images/download.svg) ](https://bintray.com/kmdupr33/RxLoader/com.philosophicalhacker.rxloader%3Arxloader/_latestVersion)

# RxLoader

`compose()` your `Observable`s and `Single`s into ones that load data that's cached across activity orientation changes without leaking your `Activity` and without you having to worry about unsubscribing.

RxLoader accomplishes this by building on top of [Android Loaders](https://developer.android.com/guide/components/loaders.html), so the library is super lightweight (only defines 47 methods).

There's a lot of data loading solutions already for Android. I detail why I felt we needed another one [here](http://www.philosophicalhacker.com/post/rxloader-boilerplate-free-data-loading-with-loaders-and-rxjava)

## Code

Github repo is [here](https://github.com/kmdupr33/RxLoader). Note that it contains [some rough sample code](https://github.com/kmdupr33/RxLoader/tree/master/sample).

## Javadoc

Coming Soon

## Basic Usage

### build.gradle

```groovy
dependencies {
  compile 'com.philosophicalhacker.rxloader:rxloader:0.4.0'
}  
```

### FooActivity

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    final RxLoader rxLoader = new RxLoader(this, getSupportLoaderManager());
    storyApiService
        .getAskStories()
        .compose(rxLoader.<List<Integer>>makeSingleTransformer())
        .subscribe(new Consumer<List<Integer>>() {
          @Override public void accept(List<Integer> integers) {
            Log.d(TAG, "call() called with: integers = [" + integers + "]");
          }
        });
}
```

Notice that you don't have worry about calling `subscribeOn` or `observeOn` either. That's handled by RxLoader.

## License

Copyright 2017 Kevin Matthew Dupree

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
