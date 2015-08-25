package com.nardix.backup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;

import org.junit.Test;

import com.nardix.backup.finddup.DupFileInfo;
import com.nardix.backup.finddup.FindDuplicatesFileVisitor;

public class BackupPCTest {
	
	//@Test
	public void testBackupFs() throws Exception {
		String backupRepo = "C:/snardi/git-nx-backup/nx-backup/data/repoDir";
		String sourceDir = "C:/snardi/git-nx-backup/nx-backup/data/sourceDir";
		BackupPC backup = new BackupPC(backupRepo, sourceDir);
		backup.backupFs();
	}
	
	//@Test
	public void testRestoreFs() throws Exception {
		String backupRepo = "C:/snardi/git-nx-backup/nx-backup/data/repoDir";
		String sourceDir = "C:/snardi/git-nx-backup/nx-backup/data/sourceDir";
		String restoreDir = "C:/snardi/git-nx-backup/nx-backup/data/restoreDir";
		BackupPC backup = new BackupPC(backupRepo, sourceDir);
		backup.restoreFs(restoreDir, 2);
	}
	
	//@Test
	public void testFindDuplicates() throws Exception {
		String backupRepo = "C:/snardi/git-nx-backup/nx-backup/data/repoDir";
		//String sourceDir = "C:/snardi/git-nx-backup/nx-backup/data/sourceDir";
		String sourceDir = "C:/snardi";
		BackupPC backup = new BackupPC(backupRepo, sourceDir);
		FindDuplicatesFileVisitor f = backup.findDuplicates();
		
		SortedSet<DupFileInfo> dupFiles = f.getDuplicateFiles();
		System.out.println("Duplicated files");
		for (DupFileInfo dupFile: dupFiles) {
			System.out.println(dupFile.file + "\t" + dupFile.md5);
		}
		System.out.println("############################################");
		System.out.println("Duplicated directories");
		TreeMap<String, List<String>> dupDirs = f.getDuplicateDirs();
		
		try (FileWriter ff = new FileWriter(new File("C:/snardi/tmp/out.txt"));
				BufferedWriter bw = new BufferedWriter(ff)) {
			for (Entry<String, List<String>> entry: dupDirs.entrySet()) {
				System.out.println(entry.getKey());
				bw.write(entry.getKey());
				for (String file: entry.getValue()) {
					System.out.println("\t\t" + file);
					bw.write("\t\t" + file);
				}
			}
		}
		
	}

}
