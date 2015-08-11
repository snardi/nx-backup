package com.nardix.backup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;

public class BackupFileVisitor implements FileVisitor {
	RepoDescriptor repo;
	boolean skipDir = true;
	int nextRevision;
	Path targetRepoDir;
	FileSystem fs = FileSystems.getDefault();
	HashMap<String, String> files = new HashMap<String, String>();
	HashSet<String> deleted = new HashSet<String>();
	HashSet<String> processed = new HashSet<String>();
	
	
	public BackupFileVisitor(RepoDescriptor r) {
		try {
			this.repo = r;
			this.nextRevision = r.getCurrentRevision() + 1;
		
			targetRepoDir = fs.getPath(repo.getRepoDir().toString(), String.format("%03d", nextRevision));
			Files.createDirectory(targetRepoDir);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public FileVisitResult preVisitDirectory(Object dir,
			BasicFileAttributes attrs) throws IOException {
//		if (skipDir) {
//			skipDir = false;
//		} else {
//			System.out.println("PRE VISIT DIR " + dir);
//			Path currentSource = (Path)dir;
//			String curr = currentSource.toString().substring(repo.getSourceDir().toString().length());
//			Path currentTarget = fs.getPath(targetRepoDir.toString(), curr);
//			
//			switch (nextRevision) {
//			case 0:
//				Files.createDirectory(currentTarget); // TODO: Preserve directory attributes
//				break;
//			default:
//				 // This directory was deleted in the past.
//				if (repo.getDeleted().contains(currentSource.toString())) {
//					
//				}
//			}
//		
//			//try { Thread.sleep(wait_milli); } catch (Exception ignore) {}
//		}
	
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Object file, BasicFileAttributes attrs) {
		try {
			String md5;
			//System.out.println("VISIT FILE " + file);

			Path currentSource = (Path) file;
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
				md5 = copy(currentSource, currentTarget);
				files.put(currentSource.toString(), md5);
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
					md5 = copy(currentSource, currentTarget);
					files.put(currentSource.toString(), md5);
					//processed.add(currentSource.toString());
				} else { // The file was once backuped and could being deleted or not.
					if (repo.getDeleted().containsKey(currentSource.toString())) {
						// Check if directory exits, otherwise create it.
						parent = currentTarget.getParent();
						if (!Files.exists(parent)) {
							Files.createDirectories(parent);
						}
						md5 = copy(currentSource, currentTarget);
						files.put(currentSource.toString(), md5);
						//processed.add(currentSource.toString());
					} else {
						md5 = md5sum(currentSource);
						String prevMd5 = repo.getFiles().get(currentSource.toString()).md5;
						if (!md5.equals(prevMd5)) {
							// Check if directory exits, otherwise create it.
							parent = currentTarget.getParent();
							if (!Files.exists(parent)) {
								Files.createDirectories(parent);
							}
							Files.copy(currentSource, currentTarget);
							files.put(currentSource.toString(), md5);
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
	public FileVisitResult visitFileFailed(Object file, IOException exc)
			throws IOException {
		System.out.println("VISIT FILE FAILED " + file);
		//try { Thread.sleep(wait_milli); } catch (Exception ignore) {}
		//throw new Exception("Failed !!!!!");
		return FileVisitResult.TERMINATE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Object dir, IOException exc)
			throws IOException {
		//System.out.println("POST VISIT DIR " + dir);
		//try { Thread.sleep(wait_milli); } catch (Exception ignore) {}
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
	
	
	private String md5sum(Path file) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte buffer[] = new byte[2048];
		try (InputStream is = Files.newInputStream(file);
				DigestInputStream dis = new DigestInputStream(is, md)) {
			while (-1 != dis.read(buffer)) {}
		}
		buffer = md.digest();
		return hex(buffer);
	}
	private String copy(Path source, Path target) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte buffer[] = new byte[2048];
		int len;
		
		try (InputStream is = Files.newInputStream(source);
				DigestInputStream dis = new DigestInputStream(is, md);
				FileOutputStream output = new FileOutputStream(target.toFile())) {
			while (-1 != (len = dis.read(buffer))) {
				output.write(buffer, 0, len);
			}
		}
		buffer = md.digest();
		return hex(buffer);
	}
	
	private String hex(byte[] digest) {
		StringBuffer hexString = new StringBuffer();

        for (int i = 0; i < digest.length; i++) {
            if ((0xff & digest[i]) < 0x10) {
                hexString.append("0");
                hexString.append(Integer.toHexString((0xFF & digest[i])));
            } else {
                hexString.append(Integer.toHexString(0xFF & digest[i]));
            }
        }
        return hexString.toString();
	}
	
	
	

}
