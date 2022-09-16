package org.thermoweb.intellij.plugin.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jetbrains.annotations.NotNull;

public class YamlUtils {

	private YamlUtils() {

	}

	@NotNull
	private static List<?> sortListItems(final ArrayList<?> value) {
		List<Map<String, Object>> sortedvalueList = new ArrayList<>();
		for (Object listValue : value) {
			if (listValue instanceof Map) {
				sortedvalueList.add(sortMapByKey((Map<String, Object>) listValue));
			} else {
				return value;
			}
		}
		return sortedvalueList;
	}

	static Map<String, Object> sortMapByKey(final Map<String, Object> map) {
		Map<String, Object> sortedMap = new TreeMap<>();
		TreeSet<String> keys = new TreeSet<>(map.keySet());
		for (String key : keys) {
			Object value = map.get(key);
			if (value instanceof Map) {
				sortedMap.put(key, sortMapByKey((Map<String, Object>) value));
			} else if (value instanceof ArrayList) {
				sortedMap.put(key, sortListItems((ArrayList<?>) value));
			} else {
				sortedMap.put(key, value);
			}
		}

		return sortedMap;
	}
}
