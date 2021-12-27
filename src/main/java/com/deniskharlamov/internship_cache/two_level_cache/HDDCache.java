package com.deniskharlamov.internship_cache.two_level_cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

public class HDDCache<K, V extends Serializable> implements ICache<K, V>, IPushingObjects<K> {

	private Map<K, String> cache;
	private Map<K, Integer> frequency;
	
	public HDDCache() {
		cache = new HashMap<>();
		frequency = new TreeMap<>();
		createHDDCache();
	}
	
	@Override
	public void put(K key, V value) throws IOException {
		String path = savePath();
		frequency.put(key, 1);
		cache.put(key, path);
		addHDD(value, path);
	}
	
	@Override
	public Optional<V> get(K key) throws ClassNotFoundException, IOException {
		if (cache.containsKey(key)) {
			String path = cache.get(key);
			int freq = frequency.get(key);
			frequency.put(key, ++freq);
			return Optional.of(getHDD(path));
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<V> remove(K key) throws ClassNotFoundException, IOException {
		if (cache.containsKey(key)) {
			V value = getHDD(cache.get(key));
			delHDD(cache.remove(key));
			frequency.remove(key);
			return Optional.of(value);
		}
		return Optional.empty();
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
	
	@Override
	public void cleanUp() {
		for (K key : cache.keySet()) {
			delHDD(cache.get(key));
		}
		cache.clear();
		frequency.clear();
	}
	
	@SuppressWarnings("unchecked")
	private V getHDD(String path) throws IOException, ClassNotFoundException {
		try (var ois = new ObjectInputStream(new FileInputStream(path))) {
			return (V) ois.readObject();
		}
	}
	
	private void addHDD(V value, String path) throws IOException {
		try(var oos = new ObjectOutputStream(new FileOutputStream(path))) {
			oos.writeObject(value);
			oos.flush();
		}
	}
	
	private void delHDD(String path) {
		new File(path).delete();
	}
	
	private String savePath() {
		return "/home/hdd_cache/" + UUID.randomUUID().toString() + ".temp";
	}
	
	private void createHDDCache() {
		File temp = new File("/home/hdd_cashe");
		if (!temp.exists()) {
			temp.mkdirs();
		}
	}
}









