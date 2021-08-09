package xyz.vecho.SimpleMinecraftLauncher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import xyz.vecho.SimpleMinecraftLauncher.util.ArchiveUtils;
import xyz.vecho.SimpleMinecraftLauncher.util.HashUtils;
import xyz.vecho.SimpleMinecraftLauncher.util.MinecraftUtils;
import xyz.vecho.SimpleMinecraftLauncher.util.OSUtils;
import xyz.vecho.SimpleMinecraftLauncher.util.StreamUtils;

public class Main {

	/** APPDATA/ROAMING */
	public static File APPDATA_FILE = new File(System.getenv("APPDATA"));
	/** GAME DIR */
	public static File GAME_DIR = new File(APPDATA_FILE, ".vecho");
	
	/** ASSETS */
	public static File ASSETS_DIR = new File(GAME_DIR, "assets");
	/** ASSETS INDEXES */
	public static File ASSETS_INDEXES_DIR = new File(ASSETS_DIR, "indexes");
	/** ASSETS OBJECTS */
	public static File ASSETS_OBJECTS_DIR = new File(ASSETS_DIR, "objects");

	/** LIBRARIES */
	public static File LIBRARIES_DIR = new File(GAME_DIR, "libraries");
	
	/** NATIVES */
	public static File NATIVES = new File(GAME_DIR, "natives"); // Cleaning and refreshing every launch
	
	/** JRE */
	public static File JRE = new File(GAME_DIR, "jre"); // built-in jre (optional / changeable in settings on launcher)
	
	/** TEMP */
	public static File TEMP = new File(GAME_DIR, "temp"); // temp files like jre lzma download
	
	/** VERSIONS */
	public static File VERSIONS = new File(GAME_DIR, "versions"); // for create
	
	/** VERSION MANIFEST */
	public static File VERSION_MANIFEST = new File(VERSIONS, "version_manifest.json");
	
	/** RESOURCE PACKS */
	public static File RESOURCE_PACKS = new File(GAME_DIR, "resourcepacks"); // for create
	
	/** SERVER RESOURCE PACKS */
	public static File SERVER_RESOURCE_PACKS = new File(GAME_DIR, "server-resource-packs"); // for create
	
	/** JRE 64 HASHES */
	public static String JRE_64_HASHES = "https://www.dropbox.com/s/xl2pht4928qu3s5/jrewin64.json?dl=1";
	
	/** JRE 32 HASHES */
	public static String JRE_32_HASHES = "https://www.dropbox.com/s/cv6emlug4dcm4ax/jrewin32.json?dl=1";

	/** JRE 64 */
	public static String JRE_64 = "https://www.dropbox.com/s/cvbg1k3n3pxzjjx/jrewin64.lzma?dl=1";
	
	/** JRE 32 */
	public static String JRE_32 = "https://www.dropbox.com/s/oqmt98usfgzxnaz/jrewin32.lzma?dl=1";
	
	/** VERSION MANIFEST */
	public static String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json";
	
	public static boolean useJre = false;
	
	public static void main(String[] args) throws Exception {
		OptionParser optionparser = new OptionParser();
		optionparser.allowsUnrecognizedOptions();
		optionparser.accepts("downloadjre");
		optionparser.accepts("usejre");
		OptionSpec<String> versionspec = optionparser.accepts("version").withRequiredArg().required();
		OptionSpec<String> usernamespec = optionparser.accepts("username").withRequiredArg().required();
		OptionSet optionset = optionparser.parse(args);
		
		System.out.println("Checking directories and files");
		if (!GAME_DIR.exists()) GAME_DIR.mkdirs();
		if (!VERSIONS.exists()) VERSIONS.mkdirs();
		if (!SERVER_RESOURCE_PACKS.exists()) SERVER_RESOURCE_PACKS.mkdirs();
		if (!RESOURCE_PACKS.exists()) RESOURCE_PACKS.mkdirs();
		if (!TEMP.exists()) TEMP.mkdirs();
		if (!ASSETS_DIR.exists()) ASSETS_DIR.mkdirs();
		if (!ASSETS_INDEXES_DIR.exists()) ASSETS_INDEXES_DIR.mkdirs();
		if (!ASSETS_OBJECTS_DIR.exists()) ASSETS_OBJECTS_DIR.mkdirs();
		if (!LIBRARIES_DIR.exists()) LIBRARIES_DIR.mkdirs();
		if (!NATIVES.exists()) NATIVES.mkdirs();
		clean(NATIVES);
		if (!VERSION_MANIFEST.exists()) StreamUtils.copy(new URL(VERSION_MANIFEST_URL).openStream(), new FileOutputStream(VERSION_MANIFEST));
		System.out.println("Directories and files checked");
		
		boolean flag = optionset.has("downloadjre") || optionset.has("usejre");
		if (flag) {
			System.out.println("Checking JRE");
			downloadJre(OSUtils.is64());
			useJre = true;
		}
		
		String version = optionset.valueOf(versionspec);
		JsonObject manifest = JsonParser.parseReader(new FileReader(VERSION_MANIFEST)).getAsJsonObject();
		JsonArray versions = manifest.getAsJsonArray("versions");
		
		boolean contains = false;
		JsonObject versionObj = null;
		for (JsonElement element : versions) {
			JsonObject versionObject = element.getAsJsonObject();
			if (versionObject.get("id").getAsString().equalsIgnoreCase(version)) {
				contains = true;
				versionObj = versionObject;
			}
		}
		
		if (!contains) {
			String versionLog = "";
			for (JsonElement element : versions) {
				JsonObject versionObject = element.getAsJsonObject();
				if (versionObject.get("type").getAsString().equalsIgnoreCase("release")) {
					versionLog = (versionLog + "   " + versionObject.get("id").getAsString()).trim();
				}
			}
			System.out.println("No such version found. Found versions: \n"+versionLog);
		} else {
			File versionDir = new File(VERSIONS, version);
			versionDir.mkdirs();
			File versionJson = new File(versionDir, version+".json");
			File versionJar = new File(versionDir, version+".jar");
			if (!versionJson.exists()) StreamUtils.copy(new URL(versionObj.get("url").getAsString()).openStream(), new FileOutputStream(versionJson));
			versionObj = JsonParser.parseReader(new FileReader(versionJson)).getAsJsonObject();
			JsonObject assetIndexObj = versionObj.getAsJsonObject("assetIndex");
			File assetIndex = new File(ASSETS_INDEXES_DIR, assetIndexObj.get("id").getAsString()+".json");
			if (!assetIndex.exists() || !assetIndexObj.get("sha1").getAsString().equalsIgnoreCase(HashUtils.sha1(assetIndex))) StreamUtils.copy(new URL(assetIndexObj.get("url").getAsString()).openStream(), new FileOutputStream(assetIndex));
			System.out.println("Checking assets for "+ version);
			MinecraftUtils.downloadAssets(assetIndex, ASSETS_OBJECTS_DIR);
			System.out.println("Assets checked for "+ version);
			System.out.println("Checking libraries for "+ version);
			MinecraftUtils.downloadLib(LIBRARIES_DIR, versionJson, version);
			System.out.println("Libraries checked for "+ version);
			System.out.println("Unpacking natives");
			MinecraftUtils.unpackNatives(version, NATIVES);
			System.out.println("Checking client jar");
			JsonObject clientDownload = versionObj.getAsJsonObject("downloads").getAsJsonObject("client");
			if (!versionJar.exists() || !clientDownload.get("sha1").getAsString().equalsIgnoreCase(HashUtils.sha1(versionJar))) StreamUtils.copy(new URL(clientDownload.get("url").getAsString()).openStream(), new FileOutputStream(versionJar));
			System.out.println("Client jar checked");
			
			System.out.println("Game launching...");
			System.out.println("TIP: To copy settings from other launchers copy the options.txt and paste in the .vecho folder");
			// launching
			String mainClass = versionObj.get("mainClass").getAsString();
			List<String> vmArgs = MinecraftUtils.generateVmArgs(version, NATIVES, versionJar);
			List<String> programArgs = MinecraftUtils.generateProgramArgs(versionJson, optionset.valueOf(usernamespec), GAME_DIR.getAbsolutePath(), ASSETS_DIR.getAbsolutePath());
			ProcessBuilder builder = new ProcessBuilder();
			
			List<String> commands = new ArrayList<>();
			
			if (useJre) {
				File bin = new File(JRE, "bin");
				File javawFile = new File(bin, "javaw.exe");
				commands.add("\""+javawFile.getAbsolutePath()+"\"");
			} else commands.add("javaw");
			
			commands.addAll(vmArgs);
			
			commands.add(mainClass);
			
			
			commands.addAll(programArgs);
			
			builder.command(commands);
			
			builder.directory(GAME_DIR);
			
			Process p = builder.start();
			while (p.isAlive()) {
				StreamUtils.copy(p.getInputStream(), System.out);
				StreamUtils.copy(p.getErrorStream(), System.err);
			}
		}
	}
	
	
	
    private static void downloadJre(boolean is64) throws Exception {
    	if (!JRE.exists() || !new File(JRE, "bin").exists() || !new File(JRE, "lib").exists()) {
    		System.out.println("Downloading JRE");
    		InputStream in = new URL(is64 ? JRE_64 : JRE_32).openStream();
    		File lzmaFile = new File(TEMP, "java.lzma");
    		lzmaFile.deleteOnExit();
    		OutputStream out = new FileOutputStream(lzmaFile);
    		StreamUtils.copy(in, out);
    		File zipFile = new File(TEMP, "java.zip");
    		zipFile.deleteOnExit();
    		ArchiveUtils.decompress(lzmaFile, zipFile);
    		ArchiveUtils.unzip(zipFile, JRE);
    	} else {
    		JsonObject hashes = JsonParser.parseString(StreamUtils.getStringFromInputStream(new URL(is64 ? JRE_64_HASHES : JRE_32_HASHES).openStream())).getAsJsonObject();
    		if (!checkJreHashes(JRE, hashes) || !checkFileExistance(JRE, hashes)) {
    			clean(JRE);
    			System.out.println("Downloading JRE");
    			InputStream in = new URL(is64 ? JRE_64 : JRE_32).openStream();
        		File lzmaFile = new File(TEMP, "java.lzma");
        		lzmaFile.deleteOnExit();
        		OutputStream out = new FileOutputStream(lzmaFile);
        		StreamUtils.copy(in, out);
        		File zipFile = new File(TEMP, "java.zip");
        		zipFile.deleteOnExit();
        		ArchiveUtils.decompress(lzmaFile, zipFile);
        		ArchiveUtils.unzip(zipFile, JRE);
    		}
    	}
    	System.out.println("JRE Checked");
    }
    
    private static void clean(File dir) {
    	for (File file : dir.listFiles()) {
    		if (file.isDirectory()) {
    			clean(file);
    			file.delete();
    		} else {
    			file.delete();
    		}
    	}
    }
    
    private static boolean checkJreHashes(File jre, JsonObject hashes) throws Exception {
    	boolean flag = true;
    	for (File file : jre.listFiles()) {
    		if (file.isDirectory() && file.getName().equals("bin") && file.getParentFile() == jre) {
    			String name = "bin/";
    			if (!checkHashes(file, hashes, name)) {
    				flag = false;
    			}
    		} else if (file.isDirectory() && file.getName().equals("lib") && file.getParentFile() == jre) {
    			String name = "lib/";
    			if (!checkHashes(file, hashes, name)) {
    				flag = false;
    			}
    		}
    	}
    	return flag;
    }
    
    private static boolean checkFileExistance(File jre, JsonObject hashes) {
    	for (String key : hashes.keySet()) {
    		File file = new File(jre, key.replaceAll("/", "\\\\"));
    		if (!file.exists()) return false;
    	}
    	return true;
    }
    
    private static boolean checkHashes(File dir, JsonObject hashes, String name) throws Exception {
    	boolean flag = true;
    	for (File file : dir.listFiles()) {
    		if (file.isDirectory()) {
    			String names = name + file.getName() + "/";
    			if (!checkHashes(file, hashes, names)) {
    				flag = false;
    			}
    		} else {
    			name += file.getName();
    			String hash = HashUtils.sha256(file);
    			String expectedHash = hashes.get(name).getAsString();
    			if (!hash.equalsIgnoreCase(expectedHash)) {
    				flag = false;
    			}
    		}
    	}
    	return flag;
    }

}
