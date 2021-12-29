package io.yx.asktask;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

public class MainTest {

    public static final Cache<String, String> cache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(10).build();

    public static void main(String[] args) {

        String key = "key1";

        System.out.println("cache.getIfPresent(key):\t" + cache.getIfPresent(key));

        String result = cache.get(key, (s) -> {
            return "aaa";
        });

        System.out.println(result);

        System.out.println("cache.getIfPresent(key):\t" + cache.getIfPresent(key));

    }


}
