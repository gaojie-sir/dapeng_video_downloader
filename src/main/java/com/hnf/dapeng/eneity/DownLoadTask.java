package com.hnf.dapeng.eneity;

import java.io.Serializable;
import java.util.List;

/**
 * @Author GJ
 * @Description TODO
 * @Date 2022/11/21 17:47
 * @Version 1.0
 */
public class DownLoadTask implements Serializable {
	private String videoName;
	private List<String> command;

	public DownLoadTask() {
	}

	public DownLoadTask(String videoName, List<String> command) {
		this.videoName = videoName;
		this.command = command;
	}

	public String getVideoName() {
		return videoName;
	}

	public void setVideoName(String videoName) {
		this.videoName = videoName;
	}

	public List<String> getCommand() {
		return command;
	}

	public void setCommand(List<String> command) {
		this.command = command;
	}

	@Override
	public String toString() {
		return "DownLoadTask{" +
				"videoName='" + videoName + '\'' +
				", command=" + command +
				'}';
	}
}
