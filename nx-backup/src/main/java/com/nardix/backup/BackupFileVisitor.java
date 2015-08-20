package com.nardix.backup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;

import com.nardix.backup.utils.Md5;

public class BackupFileVisitor implements FileVisitor<Path> {
	RepoDescriptor repo;
	boolean skipDir = true;
	int nextRevision;
	Path targetRepoDir;
	FileSystem fs = FileSystems.getDefault();
	HashMap<String, String> files = new HashMap<String, String>();
	HashSet<String> deleted = new HashSet<String>();
	HashSet<String> processed = new HashSet<String>();
	Md5 md5;
	
	
	public BackupFileVisitor(RepoDescriptor r) {
		try {
			this.repo = r;
			md5 = new Md5();
			this.nextRevision = r.getCurrentRevision() + 1;
		
			targetRepoDir = fs.getPath(repo.getRepoDir().toString(), String.format("%03d", nextRevision));
			Files.createDirectory(targetRepoDir);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public FileVisitResult preVisitDirectory(Path dir,
			BasicFileAttributes attrs) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
		try {
			String md5sum;
			Path currentSource = file; // FIXME
			String curr = currentSource.toString().substring(
					repo.getSourceDir().toString().length());
			Path currentTarget = fs.getPath(targetRepoDir.toString(), curr);

			processed.add(currentSource.toString());
			
			Path parent;
			switch (nextRevision) {
			case 0:
				parent = currentTarget.getParent();
				if (!Files.exists(parent)) {
					Files.createDirectories(parent);
				}
				md5sum = md5.copy(currentSource, currentTarget);
				files.put(currentSource.toString(), md5sum);
				//processed.add(currentSource.toString());
				break;
			default:
				// The file was never backuped.
				if (!repo.getFiles().containsKey(currentSource.toString())) {
					// Check if directory exits, otherwise create it.
					parent = currentTarget.getParent();
					if (!Files.exists(parent)) {
						Files.createDirectories(parent);
					}
					md5sum = md5.copy(currentSource, currentTarget);
					files.put(currentSource.toString(), md5sum);
					//processed.add(currentSource.toString());
				} else { // The file was once backuped and could being deleted or not.
					if (repo.getDeleted().containsKey(currentSource.toString())) {
						// Check if directory exits, otherwise create it.
						parent = currentTarget.getParent();
						if (!Files.exists(parent)) {
							Files.createDirectories(parent);
						}
						md5sum = md5.copy(currentSource, currentTarget);
						files.put(currentSource.toString(), md5sum);
						//processed.add(currentSource.toString());
					} else {
						md5sum = md5.sum(currentSource);
						String prevMd5 = repo.getFiles().get(currentSource.toString()).md5;
						if (!md5sum.equals(prevMd5)) {
							// Check if directory exits, otherwise create it.
							parent = currentTarget.getParent();
							if (!Files.exists(parent)) {
								Files.createDirectories(parent);
							}
							Files.copy(currentSource, currentTarget);
							files.put(currentSource.toString(), md5sum);
							//processed.add(currentSource.toString());
						}
					}
				}
				break;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc)
			throws IOException {
		System.out.println("VISIT FILE FAILED " + file);
		return FileVisitResult.TERMINATE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc)
			throws IOException {
		return FileVisitResult.CONTINUE;
	}

	public void commit() throws Exception {
		
		// Prepare list of deleted files.
		for (String f: repo.getFiles().keySet()) {
			if (!repo.getDeleted().containsKey(f)) {
				if (!processed.contains(f)) {
					deleted.add(f);
				}
			}
		}
		
		Path chkDir = fs.getPath(repo.getRepoDir().toString(),
				String.format("%03d" + RepoDescriptor.CHK_EXT_DIRNAME, nextRevision));
		Files.createDirectory(chkDir);
		// Write files and deleted in chkDir directory
		repo.incrementRevision();
		writeFiles(chkDir);
		writeDeleted(chkDir);
		writeCommit(chkDir);
	}
	
	private void writeCommit(Path chkDir) throws Exception {
		Path c = fs.getPath(chkDir.toString(), RepoDescriptor.COMMIT_FILENAME);
//		FileAttribute<Set<PosixFilePermission>> attr =
//				PosixFilePermissions.asFileAttribute(
//						PosixFilePermissions.fromString("r--------"));
		Files.createFile(c /*, attr*/); // FIXME: Support both windows and unix
	}

	private void writeFiles(Path dir) throws Exception { // TODO This method could be moved to RepoDescriptor
		File f = new File(dir.toString() + fs.getSeparator() + RepoDescriptor.FILES_FILENAME);
		try (FileWriter writer = new FileWriter(f);
				BufferedWriter bufferedWriter = new BufferedWriter(writer);) {
			
			for (String file : files.keySet()) {
				bufferedWriter.write(file);
				bufferedWriter.write('\t');
				bufferedWriter.write(files.get(file));
				bufferedWriter.write('\n');
			}
		}
	}
	
	private void writeDeleted(Path dir) throws Exception {
		File f = new File(dir.toString() + fs.getSeparator() + RepoDescriptor.DELETED_FILENAME);
		try (FileWriter writer = new FileWriter(f);
				BufferedWriter bufferedWriter = new BufferedWriter(writer);) {
			for (String file : deleted) {
				bufferedWriter.write(file);
				bufferedWriter.write('\n');
			}
		}
	}
}
