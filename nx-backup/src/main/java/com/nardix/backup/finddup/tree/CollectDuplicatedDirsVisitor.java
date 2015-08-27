package com.nardix.backup.finddup.tree;

import java.nio.file.FileSystem;

public class CollectDuplicatedDirsVisitor implements Visitor {
	private DupsTree tree;
	
	public CollectDuplicatedDirsVisitor(FileSystem fs, DupsTree tree) {
		//this.fs = fs;
		this.tree = tree;
	}
	
	@Override
	public void forwardVisit(Node n) {
		
		if (n.dupFileInfo != null) {
			throw new RuntimeException("This node is a leaf!!!!");
		} else {
			if (!n.duplicatedDir) {
				MarkDuplicateDirsVisitor v = new MarkDuplicateDirsVisitor(n);
				tree.transverseDeepFirst(v);
				if (v.isDuplicated()) {
					n.duplicatedDir = true;
				}
			}
		}
	}

	@Override
	public void backVisit(Node n) {
	}

	@Override
	public void visit(Node n) {
	}
}
