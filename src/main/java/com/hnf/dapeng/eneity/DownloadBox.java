package com.hnf.dapeng.eneity;

import com.hnf.dapeng.controller.MainPageController;
import com.hnf.dapeng.util.FileUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author GJ
 * @Description TODO
 * @Date 2022/11/21 16:39
 * @Version 1.0
 */
public class DownloadBox implements Runnable, Serializable {
	private String videoName;
	private List<String> command;
	private MainPageController mainPageController;
	private Label speed = new Label("等待中");
	private ProgressBar progressBar = new ProgressBar(0);
	private Button button = new Button("取消");

	private AtomicBoolean isRun = new AtomicBoolean(false);

	private AtomicBoolean cancel = new AtomicBoolean(false);

	private SimpleBooleanProperty done = new SimpleBooleanProperty(false);

	public DownloadBox(String videoName, List<String> command, MainPageController mainPageController) {
		this.videoName = videoName;
		this.command = command;
		this.mainPageController = mainPageController;
	}


	public SimpleBooleanProperty doneProperty() {
		return done;
	}

	public void setDone(boolean done) {
		this.done.set(done);
	}

	public Label getSpeed() {
		return speed;
	}

	public List<String> getCommand() {
		return command;
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public Button getButton() {
		return button;
	}

	public String getVideoName() {
		return videoName;
	}

	public boolean isRun() {
		return isRun.get();
	}

	public void cancel() {
		this.cancel.set(true);
	}

	public void setProgress(Double progress) {
		if (this.progressBar.getProgress() >= progress) {
			return;
		}
		Platform.runLater(() -> {
			this.progressBar.setProgress(progress);
		});
	}

	public void setSpeed(String speedStr) {
		Platform.runLater(() -> {
			this.speed.setText(speedStr);
		});
	}

	public boolean isDone() {
		return this.progressBar.getProgress() == 1;
	}

	private void downloadDone() {
		Platform.runLater(() -> {
			done.set(true);
			button.setText("打开视频");
			speed.setText("已完成");
			button.setOnMouseClicked(event -> {
				String videoPath = command.get(command.size() - 1);
				FileUtil.openFileManager(videoPath);
			});
		});
	}


	private void initTask() {
		Platform.runLater(() -> {
			setProgress(0.0);
			setSpeed("等待");
			button.setText("取消");
			setDone(false);
			cancel.set(false);
			isRun.set(false);
			mainPageController.setOnMouseCheckDownLoadBoxCancelButton(this);
		});
	}


	private void downloadError() {
		Platform.runLater(() -> {
			button.setText("重试");
			setSpeed("下载失败");
			setProgress(0.0);
			setDone(true);
			button.setOnMouseClicked(event -> {
				MainPageController.executorService.execute(this);
				initTask();
			});
		});
	}

	private int index = 0;

	@Override
	public void run() {
		isRun.set(true);
		if (cancel.get()) {
			System.out.println("任务已取消");
			return;
		}
		Platform.runLater(() -> {
			setSpeed("初始化中");
		});
		System.out.println("command:" + command);
		FileUtil.command(command, result -> {
			System.out.println("result = " + result);
			if (cancel.get()) {
				System.out.println("任务已取消");
				return;
			}
			if (result.contains("bitrate") && result.contains("speed")) {
				String speedByResult = getSpeedByResult(result);
				if (speedByResult != null && !speedByResult.isEmpty()) {
					setSpeed(speedByResult);
				}
			}
			if (result.contains("Opening 'crypto+")) {
				index++;
				double progress = index / 100.0;
				setProgress(progress);
			}
			if (result.contains("muxing overhead:")) {
				setProgress(1.0);
				downloadDone();
			}
			if (result.contains("HTTP error")) {
				cancel();
				downloadError();
			}
		}, cancel);
	}


	public static String getSpeedByResult(String result) {
		result = result.replaceAll("= ", "=");
		String[] s = result.split(" ");
		for (String s1 : s) {
			System.out.println("s1 = " + s1);
			if (s1.startsWith("bitrate")) {
				return s1.replaceAll("bitrate=", "");
			}
		}
		return null;
	}
}
