package com.nardix.backup;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.TreeMap;

import com.nardix.backup.utils.Md5;

public class FindDuplicatesFileVisitor implements FileVisitor<Path> {
	private class FileInfo {
		String md5;
		//TODO status
		FileInfo(String md5) {
			this.md5 = md5;
		}
	}
	private RepoDescriptor repo;
	private TreeMap<String, FileInfo> files;
	private Md5 md5;
	
	public FindDuplicatesFileVisitor(RepoDescriptor r) {
		repo = r;
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
		FileInfo fi = new FileInfo(md5.sum(file));
		files.put(file.toString(), fi);
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

	public TreeMap<String, FileInfo> getFiles() {
		return files;
	}



}
