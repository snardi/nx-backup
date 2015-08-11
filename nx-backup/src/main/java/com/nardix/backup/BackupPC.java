package com.nardix.backup;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;

import com.nardix.backup.RepoDescriptor.FileInfo;



/**
 * backuprepo
 * 
 * 
 * 
 * Repo dir structure:
 * 
 * ../sourcedir <file:contains the full path of source dir>
 * ---------------------
 * ../0/file1									/file1
 * ../0/file2									/file2
 * ../0/dir3/file4								/dir3/file4
 * ../0/dir3/file5								/dir3/file5
 * ../0.chk/files
 * 
 * ----------------------
 * ../1/file2			<<update>>
 * ../1/dir6/file7		<<file-new>>			/file2
 * ../1/dir8			<<dir-new>>				/dir3/file4
 * ../1.del/file1		<<file-deleted>>		/dir3/file5
 * ../1.chk/files								/dir6/file7
 * ../1.chk/deleted								/dir8
 * 
 * 
 * ----------------------
 * ../2/file1			<<file-new>>			/file1
 * ../2/dir8/file9		<<file-new>>			/file2
 * ../2.del/dir3		<<dir-deleted>			/dir6/file7
 * 												/dir8/file9
 * 
 * 
 * - init backup
 * 		- create sourceDirFile
 * 		- perform initial backup: /0
 * 		- create file tree
 * - increment backup
 * 		- merge between previous state tree & current FS
 * 			- if FS file or directory deleted, add deleted intem in the del tree
 * 			- if FS is directory, skip.
 * 			- if FS is file: if same skip it. if different, add modified item to the new tree
 * 
 * 
 * @author SNardi
 *
 */
public class BackupPC {
	FileSystem fs = FileSystems.getDefault();
	RepoDescriptor repoDesc;
	
	public BackupPC(String repo, String srcDir) throws Exception {
		Path sourceDir = fs.getPath(srcDir);
		Path repoDir = fs.getPath(repo);
		repoDesc = new RepoDescriptor(sourceDir, repoDir);
		repoDesc.checkSanity();
	}

	public void backupFs() throws Exception {
		// Finish without errors
		BackupFileVisitor bkpFileVisitor = new BackupFileVisitor(repoDesc); 
		Files.walkFileTree(repoDesc.getSourceDir(),
				EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
				bkpFileVisitor); //FIXME
		bkpFileVisitor.commit();
		
	}
	
	public void restoreFs(String targetPath, int revision) {
		boolean wasDeleted;
		int revToRestore = 0;
		
		if (revision < 0 || revision > this.repoDesc.getCurrentRevision()) {
			throw new RuntimeException("Invalid revision number: " + revision);
		}
		
		for (String file : repoDesc.getFiles().keySet()) {
			FileInfo fInfo = repoDesc.getFiles().get(file);
			
			for (int i: fInfo.revisions) {
				if (i > revision) break;
				revToRestore = i;
			}
			
			List<Integer> deletions = repoDesc.getDeleted().get(file);
			wasDeleted = false;
			if (deletions != null) {
				for (int d: deletions) {
					if (d > revToRestore && d <= revision) {
						wasDeleted = true;
						break;
					}
				}
			}
		
			if (!wasDeleted) {
				restoreFile(file, revToRestore, targetPath);
			}
		}
		
	}

	private void restoreFile(String file, int revToRestore, String targetPath) {
		try {
			Path from = fs.getPath(repoDesc.getRepoDir().toString(),
					String.format("%03d",  revToRestore), file.substring(repoDesc.getSourceDir().toString().length()));
			Path to = fs.getPath(targetPath, file.substring(repoDesc.getSourceDir().toString().length()));
			Files.createDirectories(to.getParent());
			Files.copy(from, to);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
