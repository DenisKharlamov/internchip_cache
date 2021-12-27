package com.deniskharlamov.internship_cache.lvl_2_cache;

/*
 * тест
 */
public class Main {

	public static void main(String[] args) {
		
		Cache<Integer, String> cache = new TwoLevelCache<>(2, 2);
		
		String val1 = "val1";
		String val2 = "val2";
		String val3 = "val3";
		String val4 = "val4";
		String val5 = "val5";
		
		cache.putCache(1, val1);
		cache.putCache(2, val2);
		cache.putCache(3, val3);
		cache.putCache(4, val4);
		
		System.out.println(cache.getCache(3));
		System.out.println(cache.getCache(4));
		System.out.println(cache.getCache(4));
		System.out.println(cache.getCache(4));
		System.out.println(cache.getCache(3));
		System.out.println(cache.getCache(1));
		System.out.println(cache.getCache(1));
		System.out.println(cache.getCache(2));
		System.out.println(cache.toString());
		cache.putCache(5, val5);
		System.out.println(cache.getCache(3));
		System.out.println(cache.getCache(4));
		System.out.println(cache.getCache(4));
		System.out.println(cache.getCache(4));
		System.out.println(cache.getCache(3));
		System.out.println(cache.getCache(1));
		System.out.println(cache.getCache(1));
		System.out.println(cache.getCache(2));
		System.out.println(cache.getCache(5));
		System.out.println(cache.toString());
	}
}
