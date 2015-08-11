package com.nardix.backup;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;

//import com.nardix.backup.filetree.FsTree;




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
	RepoDescriptor repoDesc;
	
	public static void main (String argv[]) throws Exception {
		
		String backupRepo = "C:/snardi/ws_sns/backupnx/data/repoDir";
		String sourceDir = "C:/snardi/ws_sns/backupnx/data/sourceDir";
		BackupPC backup = new BackupPC(backupRepo, sourceDir);
		backup.backupFs();
	}
	
	public BackupPC(String repo, String srcDir) throws Exception {
		FileSystem fs = FileSystems.getDefault();
		Path sourceDir = fs.getPath(srcDir);
		Path repoDir = fs.getPath(repo);
		repoDesc = new RepoDescriptor(sourceDir, repoDir);
		repoDesc.checkSanity();
	}

	private void backupFs() throws Exception {
		// Finish without errors
		BackupFileVisitor bkpFileVisitor = new BackupFileVisitor(repoDesc); 
		Files.walkFileTree(repoDesc.getSourceDir(),
				EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
				bkpFileVisitor);
		bkpFileVisitor.commit();
		

		// File sourceDirFile = new File(fileRepoDir, SOURCEDIR_FILENAME);
		// FileReader fileReader = new FileReader(sourceDirFile);
		// BufferedReader bufferedReader = new BufferedReader(fileReader);
		// String sourceDirFileName = bufferedReader.readLine();
		// FileSystem fs = FileSystems.getDefault();
		// doIncrementalBackup(repoDir, fs.getPath(sourceDirFileName));
		// }

	}

	private void doIncrementalBackup(Path repoDir, Path sourceDir) {
		// Determine current version
		// Sanity check
		//
		// Create new directories in the repository for files and deleted files
		
		// Start transversing the FS
		//	If file is "different", copy to backup dir.
		//  If file/directory does not exists, add entry in delete directory
	}

//	private void doFirstBackup() throws Exception {
//		File fileRepoDir = new File(repoDir.toFile(), SOURCEDIR_FILENAME);
//		try (	FileWriter fileWriter = new FileWriter(fileRepoDir);
//				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);) {
//			bufferedWriter.write(this.sourceDir.toString());
//			bufferedWriter.close();
//			fileWriter.close();
//		}
//		
//		Files.walkFileTree(this.sourceDir, EnumSet.noneOf(FileVisitOption.class),
//				Integer.MAX_VALUE, new BackupFileVisitor(repoDir, 0, sourceDir));
//	}

}
