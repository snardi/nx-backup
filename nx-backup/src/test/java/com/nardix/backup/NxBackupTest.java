package com.nardix.backup;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;

import org.junit.Test;

import com.nardix.backup.finddup.FindDuplicatesFileVisitor;

public class NxBackupTest {
	
	//@Test
	public void testBackupFs() throws Exception {
		String backupRepo = "C:/snardi/git-nx-backup/nx-backup/data/repoDir";
		String sourceDir = "C:/snardi/git-nx-backup/nx-backup/data/sourceDir";
		NxBackup backup = new NxBackup(backupRepo, sourceDir);
		backup.backupFs();
	}
	
	//@Test
	public void testRestoreFs() throws Exception {
		String backupRepo = "C:/snardi/git-nx-backup/nx-backup/data/repoDir";
		String sourceDir = "C:/snardi/git-nx-backup/nx-backup/data/sourceDir";
		String restoreDir = "C:/snardi/git-nx-backup/nx-backup/data/restoreDir";
		NxBackup backup = new NxBackup(backupRepo, sourceDir);
		backup.restoreFs(restoreDir, 2);
	}
	
	@Test
	public void testFindDuplicates() throws Exception {
		//String backupRepo = "/snardi/git_repos/nx-backup/nx-backup/data/repoDir";
		String sourceDir = "/snardi/git_repos/nx-backup/nx-backup/data/sourceDir";
		Path dir = Paths.get(sourceDir);
		FindDuplicatesFileVisitor f = NxBackup.findDuplicates(dir);
		f.printAll();
		
		System.out.println("###################################################################");
		System.out.println("### Duplicate directories #########################################");
		System.out.println("###################################################################");
		TreeMap<String, Vector<Path>> dups = f.getDuplicateDirs();
		for (Entry<String, Vector<Path>> e: dups.entrySet()) {
			System.out.println(e.getKey());
			for (Path p: e.getValue()) {
				if (p != null) {
					System.out.println("\t" + p.toString());
				}
			}
			
		}
		System.out.println("###################################################################");
		System.out.println("### Redundants directories #########################################");
		System.out.println("###################################################################");
		Vector<Path> redundants = f.getRedundantDirs();
		for (Path p: redundants) {
			System.out.println(p.toString());
		}
	}

}
