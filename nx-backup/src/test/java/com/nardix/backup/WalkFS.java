package com.nardix.backup;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class WalkFS {
	
	public static void main(String argv[]) throws Exception {
		WalkFS wfs = new WalkFS();
		wfs.run();
	}
	
	private void run() throws Exception {
		FileSystem fs = FileSystems.getDefault();
		Path p = fs.getPath("C:/snardi/tmp");
		
		PrintFileVisitor v = new PrintFileVisitor();
		Files.walkFileTree(p, v);
		
	}
		
	class PrintFileVisitor implements FileVisitor<Path> {

		@Override
		public FileVisitResult preVisitDirectory(Path dir,
				BasicFileAttributes attrs) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			System.out.println(file);
			return null;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc)
				throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc)
				throws IOException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	

}
	

	
