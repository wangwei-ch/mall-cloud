package com.wangwei.mall.item.demo;

import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompletableFutureDemo2 {

    @SneakyThrows
    public static void main(String[] args) {


        CompletableFuture<String> future = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                System.out.println(Thread.currentThread().getName() + "\t completableFuture" + "---------------1111------------");
                int i = 10/0;
                return 1024;
            }
            /**
             * 有返回值的 thenApply()
             * new Function<Integer, Integer>
             *     第一个Integer:  上一个任务返回结果的类型
             *     第二个Integer:  当前任务返回结果的类型
             */
        }).thenApply(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) {
                System.out.println("这是上一个任务返回结果值=" + integer + "-----------------2222---------------");
                return "这是thenApply的返回结果" + "----------------3333---------------";
            }
            /**
             * 计算完成时的回调方法 whenComplete
             */
        }).whenComplete(new BiConsumer<String, Throwable>() {
            @Override
            public void accept(String s, Throwable throwable) {
                System.out.println(s + "这是回调方法--------------4444--------------");
            }
        }).exceptionally(new Function<Throwable, String>() {
            @Override
            public String apply(Throwable throwable) {

                return "这是有异常抛出的结果 ---------------5555---------------";
            }
        });

        System.out.println(future.get());

    }
}
