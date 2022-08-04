package org.thermoweb.intellij.plugin.yaml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

/**
 * Yaml Sort Action Class.
 */
public class YamlSorter extends AnAction {

	private final DumperOptions options;
	private static final String LINE_SEPARATOR = "\n";

	public YamlSorter() {
		options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
	}

	@Override
	public void actionPerformed(@NotNull final AnActionEvent anActionEvent) {
		final Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);
		final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
		final Document document = editor.getDocument();

		Yaml yaml = new Yaml(options);

		final PsiFile data = anActionEvent.getDataContext().getData(CommonDataKeys.PSI_FILE);
		if (data != null) {
			String text = data.getText();

			List<String> headerComments = retrieveHeaderComments(text);
			Map<String, Object> yamlMap = yaml.load(text);
			Map<String, Object> sortedMap = sortMapByKey(yamlMap);

			String dump = yaml.dump(sortedMap);
			WriteCommandAction.runWriteCommandAction(project, () -> document.setText(dump));
			for (int i = headerComments.size() - 1; i >= 0; i--) {
				final String comment = headerComments.get(i);
				WriteCommandAction.runWriteCommandAction(project, () -> document.insertString(0, comment + LINE_SEPARATOR));
			}
		}
	}

	private static Map<String, Object> sortMapByKey(final Map<String, Object> map) {
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

	private static List<String> retrieveHeaderComments(final String text) {
		List<String> headerComments = new ArrayList<>();
		String[] lines = text.split(LINE_SEPARATOR);
		for (String line : lines) {
			if (line.startsWith("#")) {
				headerComments.add(line);
			} else {
				return headerComments;
			}
		}

		return Collections.emptyList();
	}

	@Override
	public void update(@NotNull final AnActionEvent anActionEvent) {
		Boolean isYamlFile = Optional.ofNullable(anActionEvent.getData(CommonDataKeys.PSI_FILE)).map(PsiFile::getFileType)
				.map(FileType::getDefaultExtension).map(YamlFile::isYamlFile).orElse(false);

		anActionEvent.getPresentation().setEnabledAndVisible(anActionEvent.getProject() != null && isYamlFile);
	}
}
