package com.nardix.backup.finddup.tree;

import java.nio.file.FileSystem;
import java.util.Vector;

public class PrintNodeVisitor implements Visitor {
	private FileSystem fs;
	Vector<String> path = new Vector<String>();
	StringBuilder pathName = new StringBuilder();
	
	public PrintNodeVisitor(FileSystem fs) {
		this.fs = fs;
	}
	
	@Override
	public void forwardVisit(Node n) {
		if (n.dupFileInfo != null) {
			throw new RuntimeException("This node is a leaf!!!!");
		} else {
			path.add(n.name);
			print(n);
		}
	}

	private void print(Node n) {
		for (String e : path) {
			pathName.append(fs.getSeparator()).append(e);
		}
		if (n.dupFileInfo != null) {
			System.out.println(n.dupFileInfo.md5 + "\t" + n.dupFileInfo.dup + "\t\t" + pathName.toString());
		} else {
			System.out.println(n.dirMd5 + "\t" + n.isDuplicatedDir + "\t" + (n.isRedundant? "R" : " ") + "\t" + pathName.toString());
		}
		pathName.setLength(0);
	}

	@Override
	public void backVisit(Node n) {
		path.remove(path.size()-1);
	}

	@Override
	public void visit(Node n) {
		if (n.dupFileInfo == null) {
			throw new RuntimeException("This node is not a leaf!!!!");
		} else {
			path.add(n.name);
			print(n);
		}
	}

}
