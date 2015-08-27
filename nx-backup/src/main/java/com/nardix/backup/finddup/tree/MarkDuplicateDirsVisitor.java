package com.nardix.backup.finddup.tree;

public class MarkDuplicateDirsVisitor implements Visitor {
	private Node current;
	private boolean duplicated = false;
	
	public MarkDuplicateDirsVisitor(Node current) {
		this.current = current;
	}
	
	@Override
	public void forwardVisit(Node n) {
		if (n.dupFileInfo != null) {
			throw new RuntimeException("This node is a leaf!!!!");
		} else {
			if (n != current) {
				if (n.dirMd5.equals(current.dirMd5)) {
					n.isDuplicatedDir = true;
					duplicated = true;
					addDuplicate(n);
				}
			}
		}
	}

	private void addDuplicate(Node n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void backVisit(Node n) {
	}

	@Override
	public void visit(Node n) {
	}
	
	public boolean isDuplicated() {
		return duplicated;
	}
}

