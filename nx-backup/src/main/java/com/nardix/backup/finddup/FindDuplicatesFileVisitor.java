package com.nardix.backup.finddup;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import com.nardix.backup.finddup.tree.CollectDuplicatedDirsVisitor;
import com.nardix.backup.finddup.tree.DupsTree;
import com.nardix.backup.finddup.tree.ComputeMd5Visitor;
import com.nardix.backup.finddup.tree.GetDuplicatedDirsVisitor;
import com.nardix.backup.finddup.tree.PrintNodeVisitor;
import com.nardix.backup.finddup.tree.Visitor;
import com.nardix.backup.utils.Md5;

public class FindDuplicatesFileVisitor implements FileVisitor<Path> {
	private TreeSet<DupFileInfo> files;
	//private HashMap<String, Vector<Path>> duplicatedDirs;
	private DupsTree dupsTree;
	private Md5 md5;
	private FileSystem fs;
	
	public FindDuplicatesFileVisitor(FileSystem fs) {
		files = new TreeSet<DupFileInfo>();
		//duplicatedDirs = new HashMap<>();
		md5 = new Md5();
		this.fs = fs;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
			throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			throws IOException {
		DupFileInfo fi;
		// Symbolic links are not files "per se", so we don't take it into
		// account for find duplicates.
		if (Files.isSymbolicLink(file)) {
			//System.out.println("SL [" + files.size() + "]\t" + file.toString());
			fi = new DupFileInfo(file.toString(), null, true);
		} else {
			//System.out.println("F  [" + files.size() + "]\t" + file.toString());
			fi = new DupFileInfo(file.toString(), md5.sum(file), false);
		}
		files.add(fi);
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

	public TreeMap<String, Vector<Path>> getDuplicateDirs() {
		GetDuplicatedDirsVisitor visitor = new GetDuplicatedDirsVisitor(fs);
		dupsTree.transverseDeepFirst(visitor);
		return visitor.getDuplicates();
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
		// Mark the duplicate files (not symbolic links).
		for (DupFileInfo f1: files) {
			if (!f1.dup && !f1.isLink) {
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
		
		// Add all files to the DupTree
		dupsTree = new DupsTree();
		for (DupFileInfo f: files) {
			dupsTree.addFile(f);
		}
		
		Visitor visitor = new ComputeMd5Visitor();
		dupsTree.transverseDeepFirst(visitor);
		
		visitor = new CollectDuplicatedDirsVisitor(fs, dupsTree);
		dupsTree.transverseDeepFirst(visitor);
	}
	
	public void printAll() {
		Visitor visitor = new PrintNodeVisitor(fs);
		dupsTree.transverseDeepFirst(visitor);
		
	}
}
