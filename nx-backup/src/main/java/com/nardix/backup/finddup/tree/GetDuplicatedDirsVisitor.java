/**
 * 
 */
package com.nardix.backup.finddup.tree;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.TreeMap;
import java.util.Vector;

/**
 * @author SNardi
 *
 */
public class GetDuplicatedDirsVisitor implements Visitor {
	private FileSystem fs;
	private Vector<String> path = new Vector<String>();
	private StringBuilder pathName = new StringBuilder();
	private TreeMap<String, Vector<Path>> dups = new TreeMap<>();
	private Vector<Path> redundants = new Vector<>();
	private boolean pruned = false;
	
	public GetDuplicatedDirsVisitor(FileSystem fs) {
		this.fs = fs;
	}

	@Override
	public void forwardVisit(Node n) {
		if (n.dupFileInfo != null) {
			throw new RuntimeException("This node is a leaf!!!!");
		} else {
			path.add(n.name);
			if (n.isDuplicatedDir || n.isRedundant) {
				addDirectory(n);
			}
		}
		
	}
	
	private void addDirectory(Node n) {
		for (String e : path) {
			pathName.append(fs.getSeparator()).append(e);
		}
		Path p = Paths.get(pathName.toString());
		pathName.setLength(0);

		if (n.isDuplicatedDir) {
			Vector<Path> dirs = dups.get(n.dirMd5);
			if (dirs == null) {
				dirs = new Vector<>();
				dirs.add(p);
				dups.put(n.dirMd5, dirs);
			} else {
				dirs.add(p);
			}
		}

		if (n.isRedundant) {
			redundants.add(p);
		}
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
		}
	}
	
	public TreeMap<String, Vector<Path>> getDuplicates() {
		if (!pruned) {
			prune(dups);
			pruned = true;
		}
		return dups;
	}
	
	public Vector<Path> getRedundants() {
		return redundants;
	}
	
	private void prune(TreeMap<String, Vector<Path>> dups) {
		Collection<Vector<Path>> vals = dups.values();
		boolean redundant = false;

		for (Vector<Path> v : vals) {
			System.out.println("[1]" + v);
			for (int i = 0; i < v.size(); i++) {
				if (v.get(i) != null) {

					for (Vector<Path> vt : vals) {
						for (Path pt : vt) {
							if (pt != null && !pt.toString().equals(v.get(i).toString())) {
								if (v.get(i).toString().startsWith(pt.toString())) {
									redundant = true;
									break;
								}
							}
						}
						if (redundant) {
							break;
						}
					}

					if (redundant) {
						v.set(i, null);
						redundant = false;
					}
				}
			}
		}
	}

}
