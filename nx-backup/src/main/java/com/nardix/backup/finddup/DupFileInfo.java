package com.nardix.backup.finddup;

public class DupFileInfo implements Comparable<DupFileInfo> {
	public final String file;
	public final String md5;
	public boolean dup = false;
	public final boolean isLink;

	DupFileInfo(String file, String md5, boolean isLink) {
		this.file = file;
		this.md5 = md5;
		this.isLink = isLink;
	}

	@Override
	public int compareTo(DupFileInfo df) {
		return this.file.compareTo(df.file);
	}
}
