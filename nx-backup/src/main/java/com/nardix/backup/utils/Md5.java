package com.nardix.backup.utils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class Md5 {
	private MessageDigest md;
	private byte buffer[];
	private static final int BUFFER_SIZE = 2048;
	
	public Md5() {
		try {
			md = MessageDigest.getInstance("MD5");
			buffer = new byte[BUFFER_SIZE];
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public String sum(Path file) {
		try {
			try (InputStream is = Files.newInputStream(file);
					DigestInputStream dis = new DigestInputStream(is, md)) {
				while (-1 != dis.read(buffer)) {}
			}
			buffer = md.digest();
			return hex(buffer);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public String hex(byte[] digest) {
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
