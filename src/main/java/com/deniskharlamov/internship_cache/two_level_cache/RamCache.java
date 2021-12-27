package com.deniskharlamov.internship_cache.two_level_cache;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeMap;

public class RamCache<K, V> implements ICache<K, V>, IPushingObjects<K>{

	private Map<K, V> cache;
	private Map<K, Integer> frequency;
	
	public RamCache() {
		cache = new HashMap<K, V>();
		frequency = new TreeMap<K, Integer>();
	}
	
	@Override
	public void put(K key, V value) {
		// при первом добавлении ставим частоту обращений равную 1
		frequency.put(key, 1);
		// добавляем объект
		cache.put(key, value);
	}
	
	@Override
	public Optional<V> get(K key) {
		if (cache.containsKey(key)) {
			int freq = frequency.get(key);
			frequency.put(key, ++freq);
			return Optional.of(cache.get(key));
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<V> remove(K key) {
		if (cache.containsKey(key)) {
			frequency.remove(key);
			return Optional.of(cache.remove(key));
		}
		return Optional.empty();
	}
	
	@Override
	public void cleanUp() {
		cache.clear();
		frequency.clear();
	}
	
	@Override
	public boolean contains(K key) {
		return cache.containsKey(key);
	}
	
	@Override
	public int size() {
		return cache.size();
	}
	
	@Override
	public Set<K> getFrequencyKeySet() {
			return frequency.keySet();
	}

	@Override
	public int getCountOfCalling(K key) {
		if (cache.containsKey(key)) {
			return frequency.get(key);
		}
		return 0;
	}
	
	
}














