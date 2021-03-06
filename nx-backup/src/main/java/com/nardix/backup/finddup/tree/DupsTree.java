package com.nardix.backup.finddup.tree;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.nardix.backup.finddup.DupFileInfo;

/**
 *  /one/two/three 
 *
 */
public class DupsTree {
	private Node root;
	private long numberOfFiles;
	
	
	public Object getNumberOfFiles() {
		return numberOfFiles;
	}

	public void addFile(DupFileInfo dupFile) {
		Vector<String> names = new Vector<String>();
		
		// FIXME: In case of FAT filesystems we don't
		// consider the drive unit.
		Path path = Paths.get(dupFile.file);
		for (int i = 0; i < path.getNameCount(); i++) {
			names.add(path.getName(i).toString());
		}
//		if (path.charAt(0) == '/') {
//			names = path.substring(1).split("/");
//		} else {
//			names = path.split("/");
//		}

		int startIdx = 1;
		if (root == null) {
			root = new Node(names.get(0));
		} else {
			if (!names.get(0).equals(root.name)) {
				throw new RuntimeException("Path does not belongs to this tree: " + path);
			}
		}
		
		Node currentNode = root;
		for (int i = startIdx; i < names.size(); i++) {
			if (i+1 == names.size()) { // Is the last name in path ?
				currentNode = currentNode.addChild(names.get(i), dupFile);
				numberOfFiles++;
			} else {
				currentNode = currentNode.addChild(names.get(i));
			}
			
		}	
	}
	
	public DupsTree getDuplicateFilesByContent() {
		return null;
	}
	
	public void transverseDeepFirst(Visitor visitor) {
		List<Node> path = new ArrayList<Node>();
		Node current;
		Node lastVisited = null;
		boolean forward = true;
		
		
		path.add(root);
		for (;path.size() > 0;) {
			current = path.get(path.size()-1);
			if (current.dupFileInfo != null) { // Leaf
				visitor.visit(current);
				lastVisited = current;
				forward = false;
				path.remove(path.size()-1);
			} else {
				if (forward) {
					visitor.forwardVisit(current);
				} else {
					visitor.backVisit(current);
				}
				Node next = current.getNextChild(lastVisited);
				if (next == null) {
					forward = false;
					lastVisited = current;
					path.remove(path.size()-1);
				} else {
					forward = true;
					lastVisited = null;
					path.add(next);
				}
			}
		}
	
	}
}

