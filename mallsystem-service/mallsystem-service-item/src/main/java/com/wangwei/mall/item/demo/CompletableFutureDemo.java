package com.wangwei.mall.item.demo;

import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompletableFutureDemo {


    @SneakyThrows
    public static void main(String[] args) {


        /**
         * 支持返回值
         */
        CompletableFuture future = CompletableFuture.supplyAsync(new Supplier<Object>() {
            @Override
            public Object get() {
                System.out.println(Thread.currentThread().getName() + "\t completableFuture----------1111");

                return 1024;
            }
        }).whenComplete(new BiConsumer<Object, Throwable>() {
            @Override
            public void accept(Object o, Throwable throwable) {
                System.out.println("--------------o=" + o.toString()+"-----------2222");
                System.out.println("--------------throwable" + throwable+"-------------3333");
            }
        }).exceptionally(new Function<Throwable, Object>() {
            @Override
            public Object apply(Throwable throwable) {

                System.out.println("throwable = "+ throwable+"-----------4444");
                return 6666;
            }
        });
        System.out.println(future.get()  +  "--------5555");

    }



}
