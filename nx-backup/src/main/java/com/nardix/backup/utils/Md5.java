package com.nardix.backup.utils;

import java.io.FileOutputStream;
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
			md.reset();
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

	public String sum(StringBuilder b) {
		try {
			byte[] sum = md.digest(b.toString().getBytes());
			return hex(sum);
		} catch (Exception e) {
			md.reset();
			throw e;
		}
	}
	
	public String copy(Path source, Path target) {
		try {
			byte buffer[] = new byte[2048];
			int len;
			try (InputStream is = Files.newInputStream(source);
					DigestInputStream dis = new DigestInputStream(is, md);
					FileOutputStream output = new FileOutputStream(
							target.toFile())) {
				while (-1 != (len = dis.read(buffer))) {
					output.write(buffer, 0, len);
				}
			}
			buffer = md.digest();
			return hex(buffer);
		} catch (Exception e) {
			md.reset();
			throw new RuntimeException(e);
		}
	}

}
