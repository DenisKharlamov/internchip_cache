package com.deniskharlamov.internship_cache.two_level_cache;

import java.io.IOException;
import java.util.Optional;

/*
 * Базовый интерфейс для любого кэша
 */
public interface ICache<K, V> {

	void put(K key, V value) throws IOException;
	Optional<V> get(K key) throws ClassNotFoundException, IOException;
	void cleanUp();
	Optional<V> remove(K key) throws ClassNotFoundException, IOException;
	boolean contains(K key);
	int size();
}
