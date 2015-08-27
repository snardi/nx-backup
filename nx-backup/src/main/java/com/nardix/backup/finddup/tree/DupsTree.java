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
	
	public void print() {
		List<Node> path = new ArrayList<Node>();
		Node current = root;
		Node lastPrinted = null;
	
		path.add(current);
		long count = 0;
		for (;;) {
			if (current.dupFileInfo != null /*current.isFile*/) {
				printPath(path);
				count++;
				lastPrinted = path.get(path.size()-1);
				path.remove(path.size()-1);
				current = path.get(path.size()-1);

				// print all files with same name
				count += printSameName(path, current, lastPrinted.name);
			} else {
				current = current.getNextChild(lastPrinted); //SNS
				lastPrinted = null;
				if (current == null) {
					//System.err.println("Leaf is not a file !!!!");
					//printPath(path);
					lastPrinted = path.get(path.size()-1);
					path.remove(path.size()-1);
					if (path.size() == 0) {
						System.out.println("Files printed: " + count);
						return;
					}
					current = path.get(path.size()-1);
				} else {
					path.add(current);
				}
			}
		}
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
	
	private long printSameName(List<Node> path, Node current, String name) {
		StringBuilder str = new StringBuilder();
		//Node file = null;
		long count = 0;

		str.append("/");
		for (Node node : path) { // path points to a directory
				str.append(node.name);
				str.append("/");
		}
		
		boolean first = true;
		for (Node c: current.childs) {
			if (c.name.equals(name)) {
				if (first)
					first = false;
				else {
					System.out.printf("%s %c %s\n", c.dupFileInfo.md5,
							(c.dupFileInfo.dup ? 'D' : ' '), str.toString() + c.name);	
					count++;
				}
					
			}
		}
		return count;
	}
	
	private void printPath(List<Node> path) {
		StringBuilder str = new StringBuilder();
		Node file = null;

		str.append("/");
		for (Node node : path) {
			if (node.childs == null /*.isFile*/) {
				str.append(node.name);
				file = node;
			} else {
				str.append(node.name);
				str.append("/");
			}
		}
		System.out.printf("%s %c %s\n", file.dupFileInfo.md5,
				(file.dupFileInfo.dup ? 'D' : ' '), str.toString());
	}
}

