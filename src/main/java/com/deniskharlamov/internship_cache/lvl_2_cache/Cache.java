package com.deniskharlamov.internship_cache.lvl_2_cache;

import java.util.Optional;

public interface Cache<K, V> {

	/*
	 * Метод для добавления элемента в кеш
	 */
	void putCache(K key, V value);
	
	/*
	 * Метод для получения элемента из кеша
	 */
	Optional<V> getCache(K key);
	
	/*
	 *  Метод для очистки кеша
	 */
	void clearCache();
}
