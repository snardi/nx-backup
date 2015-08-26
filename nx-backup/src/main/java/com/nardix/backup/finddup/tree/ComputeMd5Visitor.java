package com.nardix.backup.finddup.tree;

import com.nardix.backup.utils.Md5;

public class ComputeMd5Visitor implements Visitor {
	private Md5 md5 = new Md5();

	@Override
	public void forwardVisit(Node n) {
		System.out.println("F >> " + ((n.dupFileInfo != null) ? "\t" : "D\t") + n.name);
	}

	@Override
	public void backVisit(Node n) {
		System.out.println("B >> " + ((n.dupFileInfo != null) ? "F\t" : "D\t") + n.name);
		
		// Check if MD5 already computed for this node.
		if (n.dupFileInfo == null && n.dirMd5 != null) {
			return;
		}
		
		// If all childs has an already computed MD5, then compute the MD5 for
		// this node n.
		boolean allchildsWithMd5 = true;
		for (Node child: n.childs) {
			if (child.dupFileInfo == null && child.dirMd5 == null) {
				allchildsWithMd5 = false;
				break;
			}
		}
		
		if (allchildsWithMd5) {
			computeNodeMd5(n);
		}
	}

	private void computeNodeMd5(Node n) {
		StringBuilder childsMd5 = new StringBuilder();
		for (Node child: n.childs) {
			if (child.dupFileInfo == null) {
				childsMd5.append(child.dirMd5);
			} else {
				childsMd5.append(child.dupFileInfo.md5);
			}
		}
		n.dirMd5 = md5.sum(childsMd5);
	}

	@Override
	public void visit(Node n) {
		System.out.println("V >> " + ((n.dupFileInfo != null) ? "F\t" : "D\t") + n.name);
	}

}
