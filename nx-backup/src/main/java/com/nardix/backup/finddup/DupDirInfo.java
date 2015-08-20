package com.nardix.backup.finddup;

import java.util.List;

public class DupDirInfo implements Comparable<DupDirInfo> {
	public final String md5;
	public final List<String> dirs;

	public DupDirInfo(String md5, List<String> dirs) {
		this.md5 = md5;
		this.dirs = dirs;
	}

	@Override
	public int compareTo(DupDirInfo o) {
		return this.md5.compareTo(o.md5);
	}
}
