package com.nardix.backup.finddup.tree;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;

public class CollectDuplicatedDirsVisitor implements Visitor {
	private DupsTree tree;
	
	public CollectDuplicatedDirsVisitor(FileSystem fs, DupsTree tree) {
		this.tree = tree;
	}
	
	@Override
	public void forwardVisit(Node n) {
		if (n.dupFileInfo != null) {
			throw new RuntimeException("This node is a leaf!!!!");
		} else {
			if (!n.isDuplicatedDir) {
				MarkDuplicateDirsVisitor v = new MarkDuplicateDirsVisitor(n);
				tree.transverseDeepFirst(v);
				if (v.isDuplicated()) {        //SNS
					n.isDuplicatedDir = true;
					addDuplicate(n);
				}
			}

		}
	}

	private void addDuplicate(Node n) {
//		List<Path> paths;
//		if ((paths = dupDirs.get(n.dirMd5)) != null) {
//			paths.add(e)
//		}
		// TODO Auto-generated method stub
		
	}

	@Override
	public void backVisit(Node n) {
		if (!n.isDuplicatedDir) {
			boolean allChildDup = true;
			for (Node e: n.childs) {
				if (e.dupFileInfo == null) { // is a directory
					if (!e.isDuplicatedDir) {
						allChildDup = false;
						break;
					}
				} else { // is a file
					if (!e.dupFileInfo.dup) {
						allChildDup = false;
						break;
					}
				}
			}
			if (allChildDup) {
				n.isRedundant = true;
			}
		}
	}

	@Override
	public void visit(Node n) {
	}
	
	public TreeMap<String, List<Path>> getDupDirs() {
		return null;
		//TODO
	}
}
