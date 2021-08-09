package xyz.vecho.SimpleMinecraftLauncher.util;

public class OSUtils {
	
	public static boolean is64() {
    	if (System.getProperty("os.name").contains("Windows")) {
    	    return (System.getenv("ProgramFiles(x86)") != null);
    	} else {
    	    return (System.getProperty("os.arch").indexOf("64") != -1);
    	}
	}
    
	public static String arch() {
    	return is64() ? "64" : "32";
	}
	
}
