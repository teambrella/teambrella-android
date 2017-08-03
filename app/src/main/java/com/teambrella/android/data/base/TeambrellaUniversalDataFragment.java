package com.teambrella.android.data.base;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Universal Data Fragment
 */
public class TeambrellaUniversalDataFragment extends Fragment {

    private static final String LOG_TAG = TeambrellaUniversalDataFragment.class.getSimpleName();
    private boolean mIsStarted;
    private Queue<Integer> mBuffer = new LinkedList<>();
    private Disposable mDisposable;
    private Handler mHandler = new Handler();

    Observable<Integer> observable = Observable.create(observableEmitter -> {
        for (int i = 0; i < 1000; i++) {
            if (!observableEmitter.isDisposed()) {
                observableEmitter.onNext(i);
                Thread.sleep(3000);
            }
        }
        observableEmitter.onComplete();
    });

    PublishSubject<Integer> publisher = PublishSubject.create();
    ConnectableObservable<Integer> connectable = publisher.publish();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mDisposable = observable.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNext, this::onError, this::onComplete);
        connectable.connect();
        Log.e(LOG_TAG, "On create");
    }


    @Override
    public void onStart() {
        super.onStart();
        mHandler.post(() -> {
            mIsStarted = true;
            while (!mBuffer.isEmpty()) {
                publisher.onNext(mBuffer.poll());
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
        Log.e(LOG_TAG, "On stop");
        mIsStarted = false;
    }


    void onNext(Integer value) {
        if (mIsStarted) {
            publisher.onNext(value);
        } else {
            mBuffer.add(value);
        }
    }

    void onError(Throwable throwable) {

    }

    void onComplete() {

    }


    public Observable<Integer> getObservable() {
        return connectable;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposable.dispose();
    }
}
