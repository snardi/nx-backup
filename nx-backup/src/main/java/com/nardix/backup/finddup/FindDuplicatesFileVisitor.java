package com.nardix.backup.finddup;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.nardix.backup.utils.Md5;

public class FindDuplicatesFileVisitor implements FileVisitor<Path> {
	private class Hash {
		public String md5 = null;
	}
	
	private TreeSet<DupFileInfo> files;
	TreeMap<String, Hash> duplicatedDirs = new TreeMap<String, Hash>();
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

	public TreeMap<String, List<String>> getDuplicateDirs() {
		TreeMap<String, List<String>> dirs = new TreeMap<String, List<String>>();

		for (Entry<String, Hash> e: duplicatedDirs.entrySet()) {
			if (dirs.containsKey(e.getValue().md5)) {
				dirs.get(e.getValue().md5).add(e.getKey());
			} else {
				ArrayList<String> l = new ArrayList<String>();
				l.add(e.getKey());
				dirs.put(e.getValue().md5, l);
			}
		}
		return dirs;
	}
	
	public SortedSet<DupFileInfo> getDuplicateFiles() {
		TreeSet<DupFileInfo> ret = new TreeSet<DupFileInfo>();
		for (DupFileInfo f:files) {
			if (f.dup) { 
				ret.add(f);
			}
		}
		return ret;
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
		for (DupFileInfo f: files) {
			Path p = Paths.get(f.file).getParent();
			if (!nonDuplicatedDirs.contains(p.toString())) {
				duplicatedDirs.put(p.toString(), new Hash());
			}
		}
		nonDuplicatedDirs = null;
		for (String f: duplicatedDirs.keySet()) {
			computeHash(f, duplicatedDirs.get(f));
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