package org.thermoweb.intellij.plugin.yaml;

/**
 * Enum that lists all valid yaml file extensions.
 */
public enum YamlFile {
	YML, YAML;

	/** Determine if this extension is a valid Yaml extension.
	 * @param extension extension to test.
	 * @return true if this extension is a valid yaml extension.
	 */
	public static boolean isYamlFile(final String extension) {
		for (YamlFile yamlFile : values()) {
			if (yamlFile.name().equalsIgnoreCase(extension)) {
				return true;
			}
		}
		return false;
	}
}
