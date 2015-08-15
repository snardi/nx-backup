package com.nardix.backup;

import org.junit.Test;

public class BackupPCTest {
	
	@Test
	public void testBackup() throws Exception {
		String backupRepo = "C:/snardi/git-nx-backup/nx-backup/data/repoDir";
		String sourceDir = "C:/snardi/git-nx-backup/nx-backup/data/sourceDir";
		BackupPC backup = new BackupPC(backupRepo, sourceDir);
		backup.backupFs();
	}
	
	@Test
	public void restoreBackup() throws Exception {
		String backupRepo = "C:/snardi/git-nx-backup/nx-backup/data/repoDir";
		String sourceDir = "C:/snardi/git-nx-backup/nx-backup/data/sourceDir";
		String restoreDir = "C:/snardi/git-nx-backup/nx-backup/data/restoreDir";
		BackupPC backup = new BackupPC(backupRepo, sourceDir);
		backup.restoreFs(restoreDir, 3);
	}

}
