package xyz.vecho.SimpleMinecraftLauncher.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class StreamUtils {

    public static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }
	
    public static void copy(InputStream in, OutputStream out) throws IOException {
		int n;
		byte[] buffer = new byte[8192]; 
		while ((n = in.read(buffer)) != -1) {
			out.write(buffer, 0, n);
		}
		out.close();
    }
    
    public static void copy(InputStream in, OutputStream out, int bufferSize) throws IOException {
		int n;
		byte[] buffer = new byte[bufferSize]; 
		while ((n = in.read(buffer)) != -1) {
			out.write(buffer, 0, n);
		}
		out.close();
    }
    
}
