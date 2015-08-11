package com.nardix.backup;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.TreeSet;

public class Sandbox {
	
	private static String str;
	private static TreeSet<String> versions = new TreeSet<String>();

	public static void main(String[] args) throws Exception {
		
		String md5sum = copy(Paths.get("C:/snardi/ws_sns/backupnx/data/sourceDir/bcpkix-jdk15on-152.tar.gz"),
				Paths.get("C:/snardi/ws_sns/backupnx/data/tmp/copy.tar.gz"));
		
		System.out.println(md5sum);
		
	}
	
	static void init() {
		System.out.println("init");
		str = versions.first();
	}
	
	static boolean cond() {
		System.out.println("cond");
		return (versions.size() > 0);
	}
	
	static void inc() {
		System.out.println("inc");
		versions.remove(str);
	}
	
	static private String copy(Path source, Path target) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte buffer[] = new byte[2048];
		
		try (InputStream is = Files.newInputStream(source);
				DigestInputStream dis = new DigestInputStream(is, md)) {
			while (-1 != dis.read(buffer)) {

			}
		}
		byte[] digest = md.digest();
		return hex(digest);
	}
	
	static private String hex(byte[] digest) {
		//byte[] hash = md.digest();
		StringBuffer hexString = new StringBuffer();

        for (int i = 0; i < digest.length; i++) {
            if ((0xff & digest[i]) < 0x10) {
                hexString.append("0"
                        + Integer.toHexString((0xFF & digest[i])));
            } else {
                hexString.append(Integer.toHexString(0xFF & digest[i]));
            }
        }
        return hexString.toString();
	}

}
