package com.nardix.backup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

public class RepoDescriptor {
	private Path repoDir;
	private Path sourceDir;
	private int currentRevision = -1;
	// files and deleted represents the latest backup's revision, i.e.:
	// files contains the latest revision MD5 of all backup files.
	// deleted contains all the files once backuped, but currently deleted in the source directory.
	private HashMap<String, String> files = new HashMap<String, String>();
	private HashSet<String> deleted = new HashSet<String>();
	public static final String FILES_FILENAME = "FILES";
	public static final String DELETED_FILENAME = "DELETED";
	public static final String COMMIT_FILENAME = "COMMIT";
	public static final String CHK_EXT_DIRNAME = ".chk";
	private static final String SOURCEDIR_FILENAME = "sourcedir";

	public RepoDescriptor(Path sourceDir, Path repoDir) {
		this.repoDir = repoDir.normalize();
		this.sourceDir = sourceDir.normalize();
	}

	public void checkSanity() throws Exception {
		checkSourceDir();
		
		if (!repoDir.isAbsolute()) {
			throw new Exception("Repo directory must be absolute path.");
		}
		
		// Check we have full access.
		if (!Files.isDirectory(repoDir) || !Files.isExecutable(repoDir)
				|| !Files.isWritable(repoDir)) {
			throw new Exception("Repo repository does not exists or is not a directory or not have enough access privileges.");
		}

		// Compute the current version.
		TreeSet<String> versions = new TreeSet<String>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(repoDir,
				"???")) {
			for (Path f : stream) {
				versions.add(f.getFileName().toString());
			}
		}
		
		if (versions.size() == 0) { // Repository empty. No backups yet.
			File repoDir = new File(this.repoDir.toFile(), SOURCEDIR_FILENAME);
			try (FileWriter fileWriter = new FileWriter(repoDir);
					BufferedWriter bufferedWriter =
							new BufferedWriter(	fileWriter);) {
				bufferedWriter.write(repoDir.toString());
			}
		} else { // versions.size() > 0
			currentRevision = Integer.parseInt(versions.last());
		
			// Compute files and delete (i.e. current backup snapshot) based on all
			// previous files and delete.
			for (String str; versions.size() > 0; versions.remove(str)) {
				str = versions.first();
				addFiles(str);
				versions.remove(str);
			}
		}
	}
	
	private void addFiles(String str) throws Exception {
		FileSystem fs = FileSystems.getDefault();
		
		Path p = fs.getPath(repoDir.toString(), str + CHK_EXT_DIRNAME, "FILES");
		try (FileReader fReader = new FileReader(new File(p.toString()));
				BufferedReader reader = new BufferedReader(fReader);) {
			String line;
			while ((line = reader.readLine()) != null) {
				// System.out.println(line);
				String data[] = line.split("\t");
				files.put(data[0], data[1]);
				deleted.remove(data[0]);
			}
		}
		
		p = fs.getPath(repoDir.toString(), str + CHK_EXT_DIRNAME, "DELETED");
		try (FileReader fReader = new FileReader(new File(p.toString()));
				BufferedReader reader = new BufferedReader(fReader);) {
			String line;
			while ((line = reader.readLine()) != null) {
				deleted.add(line);
			}
		}
		
		
	}

	private void checkSourceDir() throws Exception {
		if (!sourceDir.isAbsolute()) {
			throw new Exception("Source directory must be absolute path.");
		}
		// Check we have full access.
		if (!Files.isDirectory(sourceDir) || !Files.isExecutable(sourceDir) || !Files.isReadable(sourceDir)) {
			System.err.println("Source repository does not exists or is not a directory or not have enough access privileges.");
		}
	}
	
	public Path getRepoDir() {
		return this.repoDir;
	}
	
//	public File getRepoDirFile() {
//		return this.repoDir.toFile();
//	}
	
	public Path getSourceDir() {
		return this.sourceDir;
	}
//	
//	public String getSourceDirName() {
//		return this.sourceDir.toString();
//	}

	public int getCurrentRevision() {
		return currentRevision;
	}
	
	public void incrementRevision() {
		currentRevision++;
	}
	
	public HashMap<String, String> getFiles() {
		return files;
	}
	
	public HashSet<String> getDeleted() {
		return deleted;
	}
}
