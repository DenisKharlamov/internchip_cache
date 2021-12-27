package com.deniskharlamov.internship_cache.two_level_cache;

import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/*
 * Простенький кеш - те элементы к которым происходит обращение реже
 * всего - вытесняются первыми(LFU).
 * 1 lvl - RAM
 * 2 lvl - HDD
 */
public class TwoLevelCache<K, V extends Serializable> implements ILevelCache<K, V> {

	// кеш в оперативной памяти
	private RamCache<K, V> ramCache;
	
	// кеш на HDD
	private HDDCache<K, V> hddCache;
	
	// кол-во попаданий после последнего recache()
	int numberOfRequests;
	
	// кол-во попаданий после которых происходит recache()
	int numberForRequestsForRecache;
	
	public TwoLevelCache(int numberOfRequestsForRecache) {
		this.numberForRequestsForRecache = numberOfRequestsForRecache;
		ramCache = new RamCache<>();
		hddCache = new HDDCache<>();
	}
	
	@Override
	public void put(K key, V value) throws IOException {
		ramCache.put(key, value);
	}
	
	@Override
	public Optional<V> get(K key) throws ClassNotFoundException, IOException {
		if (ramCache.contains(key)) {
			numberOfRequests++;
			if (isRecache()) {
				recache();
			}
			return ramCache.get(key);
		}
		if (hddCache.contains(key)) {
			numberOfRequests++;
			if (isRecache()) {
				recache();
			}
			return hddCache.get(key);
		}
		return Optional.empty();
	}
	
	@Override
	public void cleanUp() {
		hddCache.cleanUp();
		ramCache.cleanUp();
	}
	
	@Override
	public Optional<V> remove(K key) throws ClassNotFoundException, IOException {
		if (ramCache.contains(key)) {
			return ramCache.remove(key);
		}
		if (hddCache.contains(key)) {
			return hddCache.remove(key);
		}
		return Optional.empty();
	}
	
	@Override
	public boolean contains(K key) {
		if (ramCache.contains(key)) {
			return true;
		}
		if (hddCache.contains(key)) {
			return true;
		}
		return false;
	}
	
	@Override
	public int size() {
		return ramCache.size() + hddCache.size();
	}
	
	private boolean isRecache() {
		return numberOfRequests > numberForRequestsForRecache;
	}
	
	@Override
	public void recache() throws IOException, ClassNotFoundException {
		// опорное значение - среднее значение использований
		// элементов кеша
		int boundFrequency;
		
		// вычисление опорного значения отбрасывания объектов для ram-уровня
		boundFrequency = (int) ramCache.getFrequencyKeySet().stream()
				.mapToInt(k -> ramCache.getCountOfCalling(k))
				.average().getAsDouble();
		
		// все что ниже границы в ram отбрасываем на уровень в hdd
		for (K key : ramCache.getFrequencyKeySet()) {
			if (ramCache.getCountOfCalling(key) <= boundFrequency) {
				hddCache.put(key, ramCache.remove(key).get());
			}
		}
		
		// перебираем кеш на hdd и перекидываем в ram
		// все что больше опорного значения
		for (K key : hddCache.getFrequencyKeySet()) {
			try {
				if (hddCache.getCountOfCalling(key) > boundFrequency) {
					ramCache.put(key, hddCache.remove(key).get());
				}
			} catch (IOException ex) {
				continue;
			} catch (ClassNotFoundException ex) {
				continue;
			}
		}
		
		// далее определим опорное значение для hdd-кеша
		// и почистим его
		boundFrequency = (int) hddCache.getFrequencyKeySet().stream()
				.mapToInt(k -> hddCache.getCountOfCalling(k))
				.average().getAsDouble();
		
		for (K key : hddCache.getFrequencyKeySet()) {
			try {
				if (hddCache.getCountOfCalling(key) <= boundFrequency) {
					hddCache.remove(key);
				}
			} catch (IOException ex) {
				continue;
			} catch (ClassNotFoundException ex) {
				continue;
			}
		}
		// обнуляем кол-во попаданий
		numberOfRequests = 0;
	}
	
	@Override
	public Set<K> getFrequencyKeySet() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int getCountOfCalling(K key) {
		throw new UnsupportedOperationException();
	}
}












