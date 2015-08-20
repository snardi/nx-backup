package com.nardix.backup.finddup;

public class DupFileInfo implements Comparable<DupFileInfo> {
	public final String file;
	public final String md5;
	public boolean dup = false;

	DupFileInfo(String file, String md5) {
		this.file = file;
		this.md5 = md5;
	}

	@Override
	public int compareTo(DupFileInfo df) {
		return this.file.compareTo(df.file);
	}
}
