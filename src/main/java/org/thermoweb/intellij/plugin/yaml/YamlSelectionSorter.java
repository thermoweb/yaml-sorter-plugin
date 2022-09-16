package org.thermoweb.intellij.plugin.yaml;

import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

public class YamlSelectionSorter extends AnAction {
	private final DumperOptions options;
	//	private static final NotificationGroup STICKY_GROUP = new NotificationGroup("demo.notifications.balloon", NotificationDisplayType.BALLOON, true);
	private static final NotificationGroup STICKY_GROUP = NotificationGroupManager.getInstance().getNotificationGroup("Plugin Error");

	public YamlSelectionSorter() {
		options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setIndent(2);
		options.setIndicatorIndent(2);
		options.setIndentWithIndicator(true);
	}

	@Override
	public void actionPerformed(@NotNull final AnActionEvent event) {
		Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
		Project project = event.getRequiredData(CommonDataKeys.PROJECT);
		Document document = editor.getDocument();
		Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();

		Yaml yaml = new Yaml(options);

		String selectedText = primaryCaret.getSelectedText();
		try {
			Map<String, Object> yamlMap = yaml.load(selectedText);
			Map<String, Object> sortedMap = YamlUtils.sortMapByKey(yamlMap);

			String dump = yaml.dump(sortedMap);
			WriteCommandAction.runWriteCommandAction(project,
					() -> document.replaceString(primaryCaret.getSelectionStart(), primaryCaret.getSelectionEnd(), dump));
		} catch (ScannerException e) {
			Notification msg = new Notification(STICKY_GROUP.getDisplayId(), "Yaml sorter - error",
					"An error occurs durring parsing selection. Make sure your selection is correct before sorting it.", NotificationType.ERROR);
			msg.notify(project);
		}
	}

	@Override
	public void update(@NotNull final AnActionEvent event) {
		// Get required data keys
		Project project = event.getProject();
		Editor editor = event.getData(CommonDataKeys.EDITOR);

		Boolean isYamlFile = Optional.ofNullable(event.getData(CommonDataKeys.PSI_FILE)).map(PsiFile::getFileType).map(FileType::getDefaultExtension)
				.map(YamlFile::isYamlFile).orElse(false);

		Boolean isTextSelected = project != null && editor != null && editor.getSelectionModel().hasSelection();

		event.getPresentation().setEnabledAndVisible(event.getProject() != null && isYamlFile && isTextSelected);
	}
}
