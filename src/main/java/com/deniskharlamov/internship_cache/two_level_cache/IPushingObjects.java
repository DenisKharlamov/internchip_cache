package com.deniskharlamov.internship_cache.two_level_cache;

import java.util.Set;

/*
 * Интерфейс для получения данных, на основе которых будет происходить
 * вытеснение объектов на другие уровни.
 */
public interface IPushingObjects<K> {

	/*
	 * Метод возвращает множество которое содержит ключи находящихся в кэше
	 * элементов, отсортированное в порядке убывания
	 */
	Set<K> getFrequencyKeySet();
	
	int getCountOfCalling(K key);
}
