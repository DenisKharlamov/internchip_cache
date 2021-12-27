package com.deniskharlamov.internship_cache.lvl_2_cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;

import lombok.SneakyThrows;

/*
 * Класс представляет реализацию двухуровнего кеша:
 * 1lvl(LRU) - постоен на основе LinkedHashMap у которого переопределен метод
 * protected boolean removeEldestEntry(Map.Entry eldest) - после выталкивания
 * элемент попадает на второй уровень(LFU, будет находится на HDD), в свою 
 * очередь часто используемые элементы со 2-го уровня перемещаются на 1 уровень.
 */
public class TwoLevelCache<K, V> extends LinkedHashMap<K, V> implements Cache<K, V> {

	private static final long serialVersionUID = 1L;
	
	/*
	 * Директория для хранения элементов
	 * кеша 2-го уровня
	 */
	private final Path tempDir;

	/*
	 * Кеш второго уровня сделаем
	 */
	private final Map<K, String> lvl2;
	/*
	 * Счетчик обращений к элементам 2-го уровня
	 */
	private final Map<K, Integer> freq;

	/*
	 * Максимальный размер кеша
	 */
	private int maxSizeL1;
	private int maxSizeL2;

	// количество вставок в кеш 1lvl
	private int putCountLvl1;
	// кол-во удаленных элементов из кеша 1lvl
	private int evictionCountLvl1;

	// количество вставок в кеш 2lvl
	private int putCountLvl2;
	// кол-во удаленных элементов из кеша 2lvl
	private int evictionCountLvl2;

	// кол-во попаданий в 1 уровень
	private int hitCountLvl1;
	// кол-во попаданий во 2 уровень
	private int hitCountLvl2;
	// кол-во промахов
	private int missCount;

	@SneakyThrows
	public TwoLevelCache() {
		lvl2 = new HashMap<>();
		freq = new HashMap<>();
		this.maxSizeL1 = 16;
		this.maxSizeL2 = 16;
		this.tempDir = Files.createTempDirectory("cache");
		this.tempDir.toFile().deleteOnExit();
	}

	/*
	 * Конструктор в котором можно задать размер кеша обоих уровней
	 */
	@SneakyThrows
	public TwoLevelCache(int maxSizeLvl1, int maxSizeLvl2) {
		super(maxSizeLvl1);
		this.maxSizeL1 = maxSizeLvl1;
		this.maxSizeL2 = maxSizeLvl2;
		lvl2 = new HashMap<>(maxSizeLvl2);
		freq = new HashMap<>();
		this.tempDir = Files.createTempDirectory("cache");
		this.tempDir.toFile().deleteOnExit();
		System.out.println(tempDir);
	}

	// добавлять будем всегда в кеш 1-го уровня
	@Override
	public void putCache(K key, V value) {
		putCountLvl1++;
		put(key, value);
	}

	/*
	 * Поиск в кеше - сначала смотрим на 1 уровне, затем на 2-м если не находим
	 * возвращаем пустой Optional
	 */
	@Override
	public Optional<V> getCache(K key) {
		if (containsKey(key)) {
			hitCountLvl1++;
			return Optional.of(get(key));
		}
		if (lvl2.containsKey(key)) {
			hitCountLvl2++;
			addFreq(key);
			return Optional.of(getLvl2(key));
		}
		missCount++;
		return Optional.empty();
	}

	/*
	 * добавление объекта в кеш 2-го уровня
	 */
	@SneakyThrows
	private void putLvl2(K key, V value) {
		if (lvl2.size() >= maxSizeL2) {
			refreshLvl2();
		}
		File tmp = createTempFile();
		try (var oos = new ObjectOutputStream(new FileOutputStream(tmp))) {
			oos.writeObject(value);
			oos.flush();
			lvl2.put(key, tmp.getAbsolutePath());
			freq.put(key, 1);
			putCountLvl2++;
		}
	}

	@SuppressWarnings("unchecked")
	@SneakyThrows
	private V getLvl2(K key) {
		if (lvl2.containsKey(key)) {
			try (var ois = new ObjectInputStream(new FileInputStream(lvl2.get(key)))) {
				return (V) ois.readObject();
			}
		}
		return null;
	}

	/*
	 * удаление элемента из кеша 2-го уровня
	 */
	private void delLvl2(K key) {
		if (lvl2.containsKey(key)) {
			freq.remove(key);
			new File(lvl2.remove(key)).delete();
			evictionCountLvl2++;
		}
	}

	/*
	 * При превышении максимального размера 2-го уровня удаляем из него элементы с
	 * количеством вызовов ниже среднего
	 */
	private void refreshLvl2() {
		OptionalDouble average = freq.values().stream().mapToInt(x -> x.intValue()).average();
		if (average.isPresent()) {
			System.out.println("Average ---> " + average.getAsDouble());
			for (Map.Entry<K, Integer> entry : freq.entrySet()) {
				if (entry.getValue() <= average.getAsDouble()) {
					delLvl2(entry.getKey());
				}
			}
		} else {
			clearLvl2();
		}

		/*
		 * Возьмем некоторое условие, например если после удаления мало используемых
		 * элементов в кеше 2-го уровня осталось более 3-х элементов то перекидываем
		 * наибольший по кол-ву использований обратно в 1-й уровень
		 */
		if (lvl2.size() > 3) {
			K key = freq.keySet().stream()
					.max((x, y) -> Integer.compare(freq.get(x), freq.get(y)))
					.get();
			V value = getLvl2(key);
			if (value != null) {
				delLvl2(key);
				putCache(key, value);
			}
		}
	}

	/*
	 * Метод увеличивает статистику использований элементов кеша 2-го уровня
	 */
	private void addFreq(K key) {
		int frequency = freq.get(key);
		freq.put(key, ++frequency);
	}

	@SneakyThrows
	private File createTempFile() {
		return Files.createTempFile(tempDir, UUID.randomUUID().toString(), ".temp").toFile();
	}

	// выталкиваем с 1-го на 2-й уровень
	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
		if (size() > maxSizeL1) {
			K key = eldest.getKey();
			V value = eldest.getValue();
			putLvl2(key, value);
			evictionCountLvl1++;
			return true;
		}
		return false;
	}

	/*
	 * Очистка кеша
	 */
	@Override
	public void clearCache() {
		clearDir();
		clear();
		lvl2.clear();
		freq.clear();
		resetStat();
	}
	
	private void clearLvl2() {
		clearDir();
		lvl2.clear();
		freq.clear();
	}

	/*
	 * Метод удаляет все элементы кеша, которые хранятся на HDD в папке по умолчанию
	 */
	@SneakyThrows
	private void clearDir() {
		Files.walk(tempDir)
			.filter(Files::isRegularFile)
			.map(Path::toFile)
			.forEach(file -> file.delete());
	}

	/*
	 * Сброс статистики
	 */
	private void resetStat() {
		putCountLvl1 = 0;
		putCountLvl2 = 0;
		evictionCountLvl1 = 0;
		evictionCountLvl2 = 0;
		hitCountLvl1 = 0;
		hitCountLvl2 = 0;
		missCount = 0;
	}

	/*
	 * Переопределим метод toString для наглядного вывода работы кеша
	 */
	@Override
	public String toString() {
		int hitCount = hitCountLvl1 + hitCountLvl2;
		int accesses = hitCount + missCount;
		int hitPercent = accesses != 0 ? (100 * hitCount / accesses) : 0;
		return String.format(
				"TwoLevelCache[maxSize_lvl1=%d, maxSize_lvl2=%d, "
						+ "hits_lvl1=%d, hits_lvl2=%d, misses=%d, hitRate=%d%%]",
				maxSizeL1, maxSizeL2, hitCountLvl1, hitCountLvl2, missCount, hitPercent);
	}
}
