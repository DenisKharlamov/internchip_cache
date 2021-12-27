package com.deniskharlamov.internship_cache.two_level_cache;

import java.io.IOException;

public interface ILevelCache<K, V> extends ICache<K, V>, IPushingObjects<K>{

	void recache() throws IOException, ClassNotFoundException;
}
