package com.nardix.backup.finddup;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.TreeMap;
import java.util.TreeSet;

import com.nardix.backup.utils.Md5;

public class FindDuplicatesFileVisitor implements FileVisitor<Path> {
	private class DupFileInfo implements Comparable<DupFileInfo> {
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
	
	private class Hash {
		public String md5 = null;
//		public Hash(String md5) { this.md5 = md5; }
	}
	//private RepoDescriptor repo;
	private TreeSet<DupFileInfo> files;
	private Md5 md5;
	
	public FindDuplicatesFileVisitor(/*RepoDescriptor r*/) {
		//repo = r;
		files = new TreeSet<DupFileInfo>();
		md5 = new Md5();
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
			throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			throws IOException {
		DupFileInfo fi = new DupFileInfo(file.toString(), md5.sum(file));
		files.add(fi);
		System.out.println("Walked: " + files.size());
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc)
			throws IOException {
		return FileVisitResult.TERMINATE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc)
			throws IOException {
		return FileVisitResult.CONTINUE;
	}

	public TreeSet<DupFileInfo> getFiles() {
		return files;
	}

	public void getDuplicateFiles() {
		// TODO Auto-generated method stub
		
	}

	public void getDuplicateDirs() {
		// TODO Auto-generated method stub
		
	}

	public void commit() {
		for (DupFileInfo f1: files) {
			if (!f1.dup) {
				for (DupFileInfo f2: files) {
					if (!f2.dup && !f2.file.equals(f1.file)) {
						if (f2.md5.equals(f1.md5)) {
							f1.dup = true;
							f2.dup = true;
						}
					}
				}
			}
		}
		
		TreeSet<String> nonDuplicatedDirs = new TreeSet<String>();
		for (DupFileInfo f: files) {
			if (!f.dup) {
				Path p = Paths.get(f.file).getParent();
				nonDuplicatedDirs.add(p.toString());
			}
		}
		
		TreeMap<String, Hash> duplicatedDirs = new TreeMap<String, Hash>();
		for (DupFileInfo f: files) {
			Path p = Paths.get(f.file).getParent();
			if (!nonDuplicatedDirs.contains(p.toString())) {
				duplicatedDirs.put(p.toString(), new Hash());
			}
		}
		
		for (String f: duplicatedDirs.keySet()) {
			computeHash(f, duplicatedDirs.get(f));
		}
		
		System.out.println("Duplicated dirs");
		for (String f: duplicatedDirs.keySet()) {
			System.out.println(f + "\t" + duplicatedDirs.get(f).md5);
		}
	}
	
	
	private void computeHash(String file, Hash h) {
		StringBuilder b = new StringBuilder();
		for (DupFileInfo f:files) {
			if (f.file.startsWith(file)) {
				b.append(f.md5);
			}
		}
		h.md5 = md5.sum(b);
	}



}
