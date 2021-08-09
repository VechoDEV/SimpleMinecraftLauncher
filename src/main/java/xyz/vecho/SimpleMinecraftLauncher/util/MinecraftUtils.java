package xyz.vecho.SimpleMinecraftLauncher.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MinecraftUtils {

	public static HashMap<String, List<String>> classPathLibraries = new HashMap<>(); 
	
	public static HashMap<String, List<File>> natives = new HashMap<>();
	
	public static List<String> generateVmArgs(String version, File nativesDirectory, File clientJar) {
		if (classPathLibraries.containsKey(version)) {
			List<String> vmArgs = new ArrayList<>();
	    	vmArgs.add("-Djava.library.path="+nativesDirectory.getAbsolutePath());
	        vmArgs.add("-Dfml.ignoreInvalidMinecraftCertificates=true");
	        vmArgs.add("-Dfml.ignorePatchDiscrepancies=true");
	        vmArgs.add("-cp");
	        List<String> libs = classPathLibraries.get(version);
	        StringBuilder classpathBuilder = new StringBuilder();
	        libs.add(clientJar.getAbsolutePath());
	        Iterator<String> it = libs.iterator();
	        
	        while(it.hasNext()) {
	        	if (!it.hasNext()) {
	        		classpathBuilder.append(it.next());
	        	} else {
	        		classpathBuilder.append(it.next()+File.pathSeparator);
	        	}
	        }
	        vmArgs.add("\""+classpathBuilder.toString()+"\"");
	        return vmArgs;
		}
		return new ArrayList<>();
	}
	
	public static List<String> generateProgramArgs(File clientJson, String username, String game_dir, String assets_dir) throws IOException {
		JsonObject clientJsonObject = JsonParser.parseReader(new FileReader(clientJson)).getAsJsonObject();
		List<String> args = new ArrayList<>();
		args.add("--username");
		args.add(username);
		args.add("--version");
		args.add(clientJsonObject.get("id").getAsString());
		args.add("--gameDir");
		args.add(game_dir);
		args.add("--assetsDir");
		args.add(assets_dir);
		args.add("--assetIndex");
		args.add(clientJsonObject.getAsJsonObject("assetIndex").get("id").getAsString());
		args.add("--uuid");
		args.add("0");
		args.add("--accessToken");
		args.add("0");
		args.add("--userProperties");
		args.add("{}");
		args.add("--userType");
		args.add("legacy");
		return args;
	}
	
	public static void downloadAssets(File assetIndex, File objectsDir) throws IOException {
		FileReader reader = new FileReader(assetIndex);
		JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
		
		JsonObject obj2 = obj.get("objects").getAsJsonObject();
		for (String objectsObject : obj2.keySet()) {
			String hash = obj2.get(objectsObject).getAsJsonObject().get("hash").getAsString();
			String downloadLink = "http://resources.download.minecraft.net/" + hash.substring(0, 2) + "/"+ hash;
			File locationCopyFolder = new File(objectsDir, hash.substring(0, 2));
			File locationCopy = new File(locationCopyFolder, hash);
			if (!locationCopyFolder.exists()) locationCopyFolder.mkdirs();
			if (!locationCopy.exists() || locationCopy.length() != obj2.get(objectsObject).getAsJsonObject().get("size").getAsLong()) {
				locationCopy.createNewFile();
				InputStream stream = new URL(downloadLink).openStream();
				StreamUtils.copy(stream, new FileOutputStream(locationCopy));
			}
		}
		
		reader.close();
	}
	
	public static void downloadLib(File librariesDir, File clientJson, String version) {
		try {
			JsonObject clientJsonObject = JsonParser.parseReader(new FileReader(clientJson)).getAsJsonObject();
			JsonArray libraries = clientJsonObject.getAsJsonArray("libraries");
			if (!librariesDir.exists()) librariesDir.mkdirs();
			List<String> librariesForClassPath = new ArrayList<>();
			List<File> nativesList = new ArrayList<>();
			libraries.forEach((action2) -> {
				JsonObject library = action2.getAsJsonObject();
				String name = library.get("name").getAsString();
				String[] mavenName = name.split(":");
				String[] mavenNameArtifact = mavenName[0].split("\\.");
				mavenName = (String[]) ArrayUtils.remove(mavenName, 0);
				String fullPath = StringUtils.join(mavenNameArtifact, File.separatorChar) + File.separatorChar + StringUtils.join(mavenName, File.separatorChar);
				File dir = new File(librariesDir, fullPath);
				File jar = new File(dir, mavenName[0] + "-" + mavenName[1] + ".jar");
				if (!dir.exists()) dir.mkdirs();
				List<String> disallowed = new ArrayList<>();
				disallowed.add("osx");
				disallowed.add("windows");
				disallowed.add("linux");
				JsonArray rules = library.getAsJsonArray("rules");
				if (rules == null) {
					JsonObject downloads = library.getAsJsonObject("downloads");
					JsonObject artifact = downloads.getAsJsonObject("artifact");
					JsonObject classifiers = downloads.getAsJsonObject("classifiers");
					if (artifact != null) {
						try {
							librariesForClassPath.add(jar.getAbsolutePath());
							if (!jar.exists()) {
								InputStream stream = new URL(artifact.get("url").getAsString()).openStream();
								jar.createNewFile();
								StreamUtils.copy(stream, new FileOutputStream(jar));
							} else if (artifact.get("size").getAsLong() != jar.length()) {
								InputStream stream = new URL(artifact.get("url").getAsString()).openStream();
								StreamUtils.copy(stream, new FileOutputStream(jar));
							}
						} catch (IOException e1) {
							
							e1.printStackTrace();
						}
					}
					
					if (SystemUtils.IS_OS_WINDOWS) {
						if (classifiers != null && library.getAsJsonObject("natives") != null) {
							if (library.getAsJsonObject("natives").has("windows")) {
								JsonObject artifact_native = classifiers.getAsJsonObject(library.getAsJsonObject("natives").get("windows").getAsString().replaceAll("\\$\\{arch\\}", OSUtils.arch()));
								try {
									InputStream stream = new URL(artifact_native.get("url").getAsString()).openStream();
									File nativeJar = new File(dir, mavenName[0] + "-" + mavenName[1] + "-"+library.getAsJsonObject("natives").get("windows").getAsString().replaceAll("\\$\\{arch\\}", OSUtils.arch())+".jar");
									if (!nativeJar.exists()) {
										nativeJar.createNewFile();
										StreamUtils.copy(stream, new FileOutputStream(nativeJar));
									} else if (artifact_native.get("size").getAsLong() != nativeJar.length()) {
										StreamUtils.copy(stream, new FileOutputStream(nativeJar));
									}
									nativesList.add(nativeJar);
								} catch (IOException e1) {
									
									e1.printStackTrace();
								}
							}
						}
					} else if (SystemUtils.IS_OS_LINUX) {
						if (classifiers != null && library.getAsJsonObject("natives") != null) {
							if (library.getAsJsonObject("natives").has("linux")) {
								JsonObject artifact_native = classifiers.getAsJsonObject(library.getAsJsonObject("natives").get("linux").getAsString());
								try {
									InputStream stream = new URL(artifact_native.get("url").getAsString()).openStream();
									File nativeJar = new File(dir, mavenName[0] + "-" + mavenName[1] + "-"+library.getAsJsonObject("natives").get("linux").getAsString()+".jar");
									if (!nativeJar.exists()) {
										nativeJar.createNewFile();
										StreamUtils.copy(stream, new FileOutputStream(nativeJar));
									} else if (artifact_native.get("size").getAsLong() != nativeJar.length()) {
										StreamUtils.copy(stream, new FileOutputStream(nativeJar));
									}
									nativesList.add(nativeJar);
								} catch (IOException e1) {
									
									e1.printStackTrace();
								}
							}
						}
					} else if (SystemUtils.IS_OS_MAC) {
						if (classifiers != null && library.getAsJsonObject("natives") != null) {
							if (library.getAsJsonObject("natives").has("osx")) {
								JsonObject artifact_native = classifiers.getAsJsonObject(library.getAsJsonObject("natives").get("osx").getAsString());
								try {
									InputStream stream = new URL(artifact_native.get("url").getAsString()).openStream();
									File nativeJar = new File(dir, mavenName[0] + "-" + mavenName[1] + "-"+library.getAsJsonObject("natives").get("osx").getAsString()+".jar");
									if (!nativeJar.exists()) {
										nativeJar.createNewFile();
										StreamUtils.copy(stream, new FileOutputStream(nativeJar));
									} else if (artifact_native.get("size").getAsLong() != nativeJar.length()) {
										StreamUtils.copy(stream, new FileOutputStream(nativeJar));
									}
									nativesList.add(nativeJar);
								} catch (IOException e1) {
									
									e1.printStackTrace();
								}
							}
						}
					}
				} else {
					rules.forEach((action3) -> {
						JsonObject rule = action3.getAsJsonObject();
						if (rule.get("action").getAsString().equalsIgnoreCase("allow")) {
							if (rule.get("os") != null) {
								JsonObject oses = rule.getAsJsonObject("os");
								disallowed.remove(oses.get("name").getAsString());
							} else {
								disallowed.clear();
							}
						} else if (rule.get("action").getAsString().equalsIgnoreCase("disallow")) {
							if (rule.get("os") != null) {
								JsonObject oses = rule.getAsJsonObject("os");
								disallowed.add(oses.get("name").getAsString());
							} else {
								disallowed.add("osx");
								disallowed.add("windows");
								disallowed.add("linux");
							}
						}
					});
					
					if (SystemUtils.IS_OS_WINDOWS) {
						if (!disallowed.contains("windows")) {
							JsonObject downloads = library.getAsJsonObject("downloads");
							JsonObject artifact = downloads.getAsJsonObject("artifact");
							JsonObject classifiers = downloads.getAsJsonObject("classifiers");
							if (artifact != null) {
								try {
									librariesForClassPath.add(jar.getAbsolutePath());
									if (!jar.exists()) {
										InputStream stream = new URL(artifact.get("url").getAsString()).openStream();
										jar.createNewFile();
										StreamUtils.copy(stream, new FileOutputStream(jar));
									} else if (artifact.get("size").getAsLong() != jar.length()) {
										InputStream stream = new URL(artifact.get("url").getAsString()).openStream();
										StreamUtils.copy(stream, new FileOutputStream(jar));
									}
								} catch (IOException e1) {
									
									e1.printStackTrace();
								}
							}
							
							if (classifiers != null) {
								if (library.getAsJsonObject("natives").has("windows")) {
									JsonObject artifact_native = classifiers.getAsJsonObject(library.getAsJsonObject("natives").get("windows").getAsString().replaceAll("\\$\\{arch\\}", OSUtils.arch()));
									try {
										InputStream stream = new URL(artifact_native.get("url").getAsString()).openStream();
										File nativeJar = new File(dir, mavenName[0] + "-" + mavenName[1] + "-"+library.getAsJsonObject("natives").get("windows").getAsString().replaceAll("\\$\\{arch\\}", OSUtils.arch())+".jar");
										if (!nativeJar.exists()) {
											nativeJar.createNewFile();
											StreamUtils.copy(stream, new FileOutputStream(nativeJar));
										} else if (artifact_native.get("size").getAsLong() != nativeJar.length()) {
											StreamUtils.copy(stream, new FileOutputStream(nativeJar));
										}
										nativesList.add(nativeJar);
									} catch (IOException e1) {
										
										e1.printStackTrace();
									}
								}
							}
						}
					} else if (SystemUtils.IS_OS_LINUX) {
						if (!disallowed.contains("linux")) {
							JsonObject downloads = library.getAsJsonObject("downloads");
							JsonObject artifact = downloads.getAsJsonObject("artifact");
							JsonObject classifiers = downloads.getAsJsonObject("classifiers");
							if (artifact != null) {
								try {
									librariesForClassPath.add(jar.getAbsolutePath());
									if (!jar.exists()) {
										InputStream stream = new URL(artifact.get("url").getAsString()).openStream();
										jar.createNewFile();
										StreamUtils.copy(stream, new FileOutputStream(jar));
									} else if (artifact.get("size").getAsLong() != jar.length()) {
										InputStream stream = new URL(artifact.get("url").getAsString()).openStream();
										StreamUtils.copy(stream, new FileOutputStream(jar));
									}
								} catch (IOException e1) {
									
									e1.printStackTrace();
								}
							}
							
							if (classifiers != null && library.getAsJsonObject("natives") != null) {
								if (library.getAsJsonObject("natives").has("linux")) {
									JsonObject artifact_native = classifiers.getAsJsonObject(library.getAsJsonObject("natives").get("linux").getAsString());
									try {
										InputStream stream = new URL(artifact_native.get("url").getAsString()).openStream();
										File nativeJar = new File(dir, mavenName[0] + "-" + mavenName[1] + "-"+library.getAsJsonObject("natives").get("linux").getAsString()+".jar");
										if (!nativeJar.exists()) {
											nativeJar.createNewFile();
											StreamUtils.copy(stream, new FileOutputStream(nativeJar));
										} else if (artifact_native.get("size").getAsLong() != nativeJar.length()) {
											StreamUtils.copy(stream, new FileOutputStream(nativeJar));
										}
										nativesList.add(nativeJar);
									} catch (IOException e1) {
										
										e1.printStackTrace();
									}
								}
							}
						}
					} else if (SystemUtils.IS_OS_MAC) {
						if (!disallowed.contains("osx")) {
							JsonObject downloads = library.getAsJsonObject("downloads");
							JsonObject artifact = downloads.getAsJsonObject("artifact");
							JsonObject classifiers = downloads.getAsJsonObject("classifiers");
							if (artifact != null) {
								try {
									librariesForClassPath.add(jar.getAbsolutePath());
									if (!jar.exists()) {
										InputStream stream = new URL(artifact.get("url").getAsString()).openStream();
										jar.createNewFile();
										StreamUtils.copy(stream, new FileOutputStream(jar));
									} else if (artifact.get("size").getAsLong() != jar.length()) {
										InputStream stream = new URL(artifact.get("url").getAsString()).openStream();
										StreamUtils.copy(stream, new FileOutputStream(jar));
									}
								} catch (IOException e1) {
									
									e1.printStackTrace();
								}
							}
							
							if (classifiers != null && library.getAsJsonObject("natives") != null) {
								if (library.getAsJsonObject("natives").has("osx")) {
									JsonObject artifact_native = classifiers.getAsJsonObject(library.getAsJsonObject("natives").get("osx").getAsString());
									try {
										InputStream stream = new URL(artifact_native.get("url").getAsString()).openStream();
										File nativeJar = new File(dir, mavenName[0] + "-" + mavenName[1] + "-"+library.getAsJsonObject("natives").get("osx").getAsString()+".jar");
										if (!nativeJar.exists()) {
											nativeJar.createNewFile();
											StreamUtils.copy(stream, new FileOutputStream(nativeJar));
										} else if (artifact_native.get("size").getAsLong() != nativeJar.length()) {
											StreamUtils.copy(stream, new FileOutputStream(nativeJar));
										}
										nativesList.add(nativeJar);
									} catch (IOException e1) {
										
										e1.printStackTrace();
									}
								}
							}
						}
					}
				}
			});
			classPathLibraries.put(version, librariesForClassPath);
			natives.put(version, nativesList);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void unpackNatives(String version, File target) {
		if (natives.containsKey(version)) {
			List<File> nativesFiles = natives.get(version);
			for (File nativeFile : nativesFiles) {
				try {
					ArchiveUtils.unzip(nativeFile, target, "META-INF/");
				} catch (IOException e) {
					System.out.println("Cannot unpack natives");
				}
			}
		}
	}
	
}
