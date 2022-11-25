package com.hnf.dapeng.eneity;

import com.hnf.dapeng.util.FileUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author GJ
 * @Description TODO
 * @Date 2022/11/18 17:14
 * @Version 1.0
 */

public class Video extends VBox {
	private String videoName;
	private String vid;
	private String duration;
	private int definition = VideoDefinition.HD1.getDefinition();
	private Label button = new Label("下载");

	private HBox hBox = new HBox();
	private CheckBox checkBox = new CheckBox();

	public Video(String videoName, String vid, String duration) {
		this.videoName = videoName;
		this.vid = vid;
		this.duration = duration;
		init();
	}

	public void setDefinition(int definition) {
		this.definition = definition;
	}

	public CheckBox getCheckBox() {
		return checkBox;
	}

	private void init() {
		button.setStyle("-fx-border-width: 0 0 1 0;-fx-border-color: black;-fx-cursor: hand");
		button.setMinWidth(28);
		Label label = new Label(videoName + " (" + duration + ")");
		label.setWrapText(true);
		hBox.getChildren().addAll(checkBox, button, label);
		hBox.setAlignment(Pos.CENTER_LEFT);
		HBox.setMargin(label, new Insets(0, 0, 0, 8));
		HBox.setMargin(button, new Insets(0, 0, 0, 2));
		HBox.setMargin(checkBox, new Insets(0, 0, 0, 2));
		this.getChildren().addAll(hBox);
		this.setSpacing(4);
		this.setMinHeight(48);
		VBox.setMargin(button, new Insets(0, 0, 0, 220));
	}

	public Label getButton() {
		return button;
	}

	public String getVideoName() {
		return videoName;
	}

	public void showCheckBox(boolean show) {
		this.checkBox.setVisible(show);
	}

	public boolean isSelected() {
		return this.checkBox.isSelected();
	}


	public List<String> getCommand() {
		vid = vid.replace("_e", "");
		String substring = vid.substring(0, 10);
		File file = new File("./ffmpeg/bin/ffmpeg.exe");
		String storePath = FileUtil.getStorePath();
		if (storePath == null || !new File(storePath).exists()) {
			return null;
		}
		try {
			String saveFile = new File(storePath).getCanonicalPath() + "/" + videoName + ".mp4";
			String ffmpegExe = file.getCanonicalPath();
			List<String> commands = new ArrayList<>();
			commands.add(ffmpegExe);
			commands.add("-user_agent");
			commands.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
			commands.add("-i");
			commands.add("https://hls.videocc.net/" + substring + "/f/" + vid + "_" + definition + ".m3u8?device=desktop&pid=1668996969401X1417421");
			commands.add("-c");
			commands.add("copy");
			commands.add("-bsf:a");
			commands.add("aac_adtstoasc");
			commands.add("-y");
			commands.add(saveFile);
			return commands;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	public enum VideoDefinition {
		HD3(3, "原画"),
		HD2(2, "超清"),
		HD1(1, "标准"),
		;
		int definition;
		String name;

		VideoDefinition(int definition, String name) {
			this.definition = definition;
			this.name = name;
		}

		public int getDefinition() {
			return definition;
		}

		public void setDefinition(int definition) {
			this.definition = definition;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
