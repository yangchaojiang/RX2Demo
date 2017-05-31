package com.example.yangc.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test7();
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscription.request(1);
            }
        });

    }

    /****
     * Observable
     * 游和下游就分别对应着RxJava中的Observable和Observer，它们之间的连接就对应着 subscribe()，因此这个关系用RxJava来表示就
     **/
    private void test() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                for (int i = 0; i < 1000; i++) {
                    e.onNext(i);
                }
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.d(TAG, integer + "");
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });

    }

    /****
     * map 测试
     *
     * @deprecated map中的函数作用是将圆形事件转换为矩形事件, 从而导致下游接收到的事件就变为了矩形 类型转换
     ***/
    private void test2() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                for (int i = 0; i < 1000; i++) {
                    e.onNext(i);
                }
            }
        }).map(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) throws Exception {
                return "String " + integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String integer) throws Exception {
                Log.d(TAG, integer + "");
            }
        });

    }

    /****
     * 测试 flatMap 和  concatMap 区别 concatMap有序 flatMap 无需
     *
     * @deprecated flatMap都将创建一个新的水管(Observable), 然后发送转换之后的新的事件, 下游（subscribe）接收到的就是这些新的水管发送的数据.
     ***/
    private void test3() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                for (int i = 0; i < 1000; i++) {
                    e.onNext(i);
                }
            }
        }).concatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                final List<String> list = new ArrayList<>();
                for (int i = 0; i < 50; i++) {
                    list.add(integer + "_" + i);
                    //    Log.d(TAG,"ObservableSource"+integer+"_"+i+"");
                }
                // return Observable.just(list);
                return Observable.fromIterable(list).delay(20, TimeUnit.MILLISECONDS);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String integer) throws Exception {
                Log.d(TAG, integer + "");
            }
        });

    }

    /***
     * zip
     *
     * @deprecated 通过一个函数将多个Observable发送的事件结合到一起，然后发送这些组合到一起的事件
     * . 它按照严格的顺序应用这个函数。它只发射与发射数据项最少的那个Observable一样多的数据。
     * 注意默认不在线程处理，谁在前面先处理  ，指定线程后完成不同操作
     **/
    private void test4() {
/*        Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.d(TAG, "emit 1");
                emitter.onNext(1);
                Thread.sleep(1000);
                Log.d(TAG, "emit 2");
                emitter.onNext(2);
                Thread.sleep(1000);
                Log.d(TAG, "emit 3");
                emitter.onNext(3);
                Thread.sleep(1000);
                Log.d(TAG, "emit 4");
                emitter.onNext(4);
                Thread.sleep(1000);
                Log.d(TAG, "emit complete1");
                emitter.onComplete();

            }
        }).subscribeOn(Schedulers.io());
        Observable<String> observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Log.d(TAG, "emit A");
                emitter.onNext("A");
                Thread.sleep(1000);
                Log.d(TAG, "emit B");
                emitter.onNext("B");
                Thread.sleep(1000);
                Log.d(TAG, "emit C");
                emitter.onNext("C");
                Thread.sleep(1000);
                Log.d(TAG, "emit complete2");
                emitter.onComplete();

            }
        }).subscribeOn(Schedulers.io());
        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                return integer+s;
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe ");
            }

            @Override
            public void onNext(String value) {
                Log.d(TAG, "accept "+value);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError ");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete ");
            }
        });*/
        Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.d(TAG, "emit 1");
                emitter.onNext(1);
                Thread.sleep(1000);
                Log.d(TAG, "emit 2");
                emitter.onNext(2);
                Thread.sleep(1000);
                Log.d(TAG, "emit 3");
                emitter.onNext(3);
                Thread.sleep(1000);
                Log.d(TAG, "emit 4");
                emitter.onNext(4);
                Thread.sleep(1000);
                Log.d(TAG, "emit complete1");
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io());
        Observable<String> observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Log.d(TAG, "emit A");
                emitter.onNext("A");
                Thread.sleep(1000);
                Log.d(TAG, "emit B");
                emitter.onNext("B");
                Thread.sleep(1000);
                Log.d(TAG, "emit C");
                emitter.onNext("C");
                Thread.sleep(1000);
                Log.d(TAG, "emit complete2");
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io());
        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                return integer + s;
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe");
            }

            @Override
            public void onNext(String value) {
                Log.d(TAG, "onNext: " + value);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete");
            }
        });
    }

    /***
     * Backpressure
     *
     * @deprecated 所谓的Backpressure其实就是为了zip  无限循环发送事件, 第二根水管随便发送点什么, 由于我们没有发送Complete事件, 因此第一根水管会一直发事件到它对应的水
     ****/
    private void test5() {
        Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.d(TAG, "emit 1");
                for (int i = 0; ; i++) {
                    emitter.onNext(i);
                }
            }
        }).subscribeOn(Schedulers.io());

        Observable<String> observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext("A");
            }
        }).subscribeOn(Schedulers.io());

        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                return integer + s;
            }
        }).sample(20, TimeUnit.SECONDS)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.d(TAG, "" + s);
                    }
                });
    }

    /**
     * Flowable
     *
     * deprecated 首先第一个同步的代码, 为什么上游发送第一个事件后下游就抛出了MissingBackpressureException异常,
     * 这是因为下游没有调用request, 上游就认为下游没有处理事件的能力, 而这又是一个同步的订阅, 既然下游处理不了,
     * 那上游不可能一直等待吧, 如果是这样, 万一这两根水管工作在主线程里, 界面不就卡死了吗, 因此只能抛个异常来提醒我们.
     * 那如何解决这种情况呢, 很简单啦, 下游直接调用request(Long.MAX_VALUE)就行了, 或者根据上游发送事件的数量来request就行了, 比如这里request(3)就可以了.
     * 第二段异步代码, 为什么上下游没有工作在同一个线程时, 上游却正确的发送了所有的事件呢?
     * 这是因为在Flowable里默认有一个大小为128的水缸, 当上下游工作在不同的线程中时,
     * 上游就会先把事件发送到这个水缸中, 因此, 下游虽然没有调用request, 但是上游在水缸中保存着这些事件,
     * 只有当下游调用request时, 才从水缸里取出事件发给下游.
     ***/
    public void test6() {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                Log.d(TAG, "emit 1");
                emitter.onNext(1);
                Log.d(TAG, "emit 2");
                emitter.onNext(2);
                Log.d(TAG, "emit 3");
                emitter.onNext(3);
                Log.d(TAG, "emit complete");
                emitter.onComplete();

            }
        }, BackpressureStrategy.ERROR).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                Log.d(TAG, "onSubscribe");
                //   s.request(Long.MAX_VALUE);//注意这句代码
                subscription = s;
            }

            @Override
            public void onNext(Integer s) {
                Log.d(TAG, "onNext:" + s);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });


    }
    /**
     * Flowable
     *
     * deprecated  说它采用了响应式拉取的方式，我们还举了个叶问打小日本的例子，再来回顾一下吧，我们说把上游看成小日本,
     * 把下游当作叶问, 当调用 Subscription.request(1) 时, 叶问就说我要打一个! 然后小日本就拿出一个鬼子给叶问, 让他打,
     * 等叶问打死这个鬼子之后, 再次调用 request(10), 叶问就又说我要打十个!
     * 然后小日本又派出十个鬼子给叶问, 然后就在边上看热闹, 看叶问能不能打死十个鬼子, 等叶问打死十个鬼子后再继续要鬼子接着打
     ***/
    public void test7() {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                Log.d(TAG, "current requested: " + emitter.requested());
                Log.d(TAG, "emit 1");
                emitter.onNext(1);
                Log.d(TAG, "emit 2");
                emitter.onNext(2);
                Log.d(TAG, "emit 3");
                emitter.onNext(3);
                Log.d(TAG, "emit complete");
                emitter.onComplete();

            }
        }, BackpressureStrategy.ERROR).subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                Log.d(TAG, "onSubscribe");
                //   s.request(Long.MAX_VALUE);//注意这句代码
                  subscription = s;
                subscription.request(100);
            }

            @Override
            public void onNext(Integer s) {
                Log.d(TAG, "onNext:" + s);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });


    }

}
