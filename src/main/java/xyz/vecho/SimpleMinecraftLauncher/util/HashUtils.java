package xyz.vecho.SimpleMinecraftLauncher.util;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;

import javax.xml.bind.DatatypeConverter;

public class HashUtils {

	public static String sha256(File file) throws Exception {
		byte[] fileBytes = Files.readAllBytes(file.toPath());
		byte[] hash = MessageDigest.getInstance("sha-256").digest(fileBytes);
		return DatatypeConverter.printHexBinary(hash);
	}
	
	public static String sha1(File file) throws Exception {
		byte[] fileBytes = Files.readAllBytes(file.toPath());
		byte[] hash = MessageDigest.getInstance("sha-1").digest(fileBytes);
		return DatatypeConverter.printHexBinary(hash);
	}
	
}
