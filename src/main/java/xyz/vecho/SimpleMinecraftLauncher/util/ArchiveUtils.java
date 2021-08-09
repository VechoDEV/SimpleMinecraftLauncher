package xyz.vecho.SimpleMinecraftLauncher.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import lzma.sdk.lzma.Decoder;
import lzma.streams.LzmaInputStream;

public class ArchiveUtils {

	public static void decompress(File lzma, File target) throws IOException {
        try (LzmaInputStream inputStream = new LzmaInputStream(
                new BufferedInputStream(new FileInputStream(lzma)),
                new Decoder());
             OutputStream outputStream = new BufferedOutputStream(
                     new FileOutputStream(target)))
        {
            StreamUtils.copy(inputStream, outputStream);
        }
	}
	
    public static void unzip(File zipFile, File target) throws IOException {
        if (!target.exists()) {
        	target.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry entry = zipIn.getNextEntry();
        while (entry != null) {
            String filePath = target + File.separator + entry.getName();
            if (!entry.isDirectory()) {
            	BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
            	StreamUtils.copy(zipIn, bos, 4096);
            } else {
                File dir = new File(filePath);
                dir.mkdirs();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
	
    public static void unzip(File zipFile, File target, String... exclude) throws IOException {
        if (!target.exists()) {
        	target.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry entry = zipIn.getNextEntry();
        while (entry != null) {
        	boolean flag = true;
        	for (String e : exclude) {
        		if (entry.getName().startsWith(e)) flag = false;
        	}
        	if (flag) {
        		String filePath = target + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                	BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
                	StreamUtils.copy(zipIn, bos, 4096);
                } else {
                    File dir = new File(filePath);
                    dir.mkdirs();
                }
        	}
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
}
