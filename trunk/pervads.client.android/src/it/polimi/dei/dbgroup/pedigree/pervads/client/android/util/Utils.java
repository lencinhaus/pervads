package it.polimi.dei.dbgroup.pedigree.pervads.client.android.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;

public final class Utils {
	public static final boolean checkServiceRunning(Context c,
			Class<? extends Service> cls) {
		ComponentName service = new ComponentName(c, cls);
		ActivityManager am = (ActivityManager) c
				.getSystemService(Context.ACTIVITY_SERVICE);
		if (am == null)
			throw new RuntimeException(
					"Cannot check running services: ActivityManager is not available");
		for (ActivityManager.RunningServiceInfo runningService : am
				.getRunningServices(Integer.MAX_VALUE)) {
			if (runningService.service.equals(service))
				return true;
		}
		return false;
	}

	public static String convertToQuotedString(String string) {
		if (TextUtils.isEmpty(string))
			return "";

		final int lastPos = string.length() - 1;
		if (lastPos < 0
				|| (string.charAt(0) == '"' && string.charAt(lastPos) == '"')) {
			return string;
		}

		return "\"" + string + "\"";
	}

	public static boolean isQuotedString(String string) {
		if (TextUtils.isEmpty(string))
			return false;

		if (string.length() >= 2 && string.charAt(0) == '"'
				&& string.charAt(string.length() - 1) == '"')
			return true;
		return false;
	}

	public static String convertToUnquotedString(String string) {
		if (TextUtils.isEmpty(string))
			return "";

		if (!isQuotedString(string))
			return string;

		return string.substring(1, string.length() - 1);
	}

	public static boolean isHexString(String key) {
		for (int i = key.length() - 1; i >= 0; i--) {
			final char c = key.charAt(i);
			if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
					&& c <= 'f')) {
				return false;
			}
		}

		return true;
	}

	public static final String formatIPAddress(int ip) {
		short[] bytes = new short[] { (short) (ip & 0xff),
				(short) ((ip >> 8) & 0xff), (short) ((ip >> 16) & 0xff),
				(short) ((ip >> 24) & 0xff) };

		StringBuilder sb = new StringBuilder(16);
		for (int i = 0; i < bytes.length; i++) {
			if (i > 0)
				sb.append('.');
			sb.append(Short.toString(bytes[i]));
		}
		return sb.toString();
	}

	public static final int parseIPAddress(String address) {
		if (TextUtils.isEmpty(address))
			throw new IllegalArgumentException(
					"address cannot be null or empty");
		String[] tokens = address.split("\\.");
		boolean isOk = true;
		int ip = 0;
		if (tokens.length == 4) {
			for (int i = 0; i < 4; i++) {
				try {
					short num = Short.parseShort(tokens[i]);
					if (num < 0 || num > 255) {
						isOk = false;
						break;
					}
					ip |= (num << (8 * i));
				} catch (NumberFormatException ex) {
					isOk = false;
					break;
				}
			}
		} else
			isOk = false;

		if (!isOk)
			throw new IllegalArgumentException(
					"address is not in the form X.X.X.X, with X in the range 0-255");

		return ip;
	}

	private static final int RANDOM_STRIN_MIN_DEFAULT = 100;
	private static final int RANDOM_STRIN_MAX_DEFAULT = 1000;
	private static final Random randomGenerator = new Random();

	public static final String generateRandomString() {
		return generateRandomString(RANDOM_STRIN_MIN_DEFAULT,
				RANDOM_STRIN_MAX_DEFAULT);
	}

	public static final String generateRandomString(int min, int max) {
		if (min < 0)
			throw new IllegalArgumentException(
					"min must be greater than or equal to 0");
		if (max < 1)
			throw new IllegalArgumentException("max must be greater than 0");
		if (max <= min)
			throw new IllegalArgumentException("max must be greater than min");
		final int chars = 'z' - 'a';
		final int numChars = randomGenerator.nextInt(max - min) + min;
		StringBuilder sb = new StringBuilder(numChars);
		for (int i = 0; i < numChars; i++)
			sb.append((char) ('a' + randomGenerator.nextInt(chars)));
		return sb.toString();
	}
	
	public static void recursiveDelete(File file) {
		recursiveDelete(file, false);
	}

	public static void recursiveDelete(File file, boolean onlyDeleteChildren) {
		if(!file.exists()) return;
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				recursiveDelete(child, false);
			}
		}
		if (!onlyDeleteChildren && !file.delete())
			throw new RuntimeException("Cannot delete file "
					+ file.getAbsolutePath());
	}

	public static void unzip(File zipFile, File destFolder) {
		try {
			unzip(new FileInputStream(zipFile), destFolder);
		} catch (Exception ex) {
			throw new RuntimeException("Cannot unzip "
					+ zipFile.getAbsolutePath() + " to " + destFolder.getAbsolutePath(),
					ex);
		}
	}

	public static void unzip(InputStream input, File destFolder) {
		try {
			byte[] buffer = new byte[4096];
			int read;
			ZipInputStream is = new ZipInputStream(input);
			ZipEntry entry;
			while ((entry = is.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					String fileName = entry.getName();
					File fileFolder = destFolder;
					int lastSep = entry.getName().lastIndexOf(
							File.separatorChar);
					if (lastSep != -1) {
						String dirPath = fileName.substring(0, lastSep);
						fileFolder = new File(fileFolder, dirPath);
						fileName = fileName.substring(lastSep + 1);
					}
					fileFolder.mkdirs();
					File file = new File(fileFolder, fileName);
					FileOutputStream os = new FileOutputStream(file);
					while ((read = is.read(buffer)) != -1) {
						os.write(buffer, 0, read);
					}
					os.flush();
					os.close();
				}
			}
			is.close();
		} catch (Exception ex) {
			throw new RuntimeException("Cannot unzip stream to "
					+ destFolder.getAbsolutePath(), ex);
		}
	}
	
	public static void moveAll(File fromFolder, File toFolder) {
		moveAll(fromFolder, toFolder, false, false);
	}
	
	public static void moveAll(File fromFolder, File toFolder, boolean overwrite, boolean clearDestinationFolder) {
		if(!fromFolder.exists()) throw new RuntimeException("From folder " + fromFolder.getAbsolutePath() + " does not exist");
		if(!toFolder.exists()) toFolder.mkdirs();
		else if(clearDestinationFolder) recursiveDelete(toFolder, true);
		for(File fromFile : fromFolder.listFiles()) {
			File toFile = new File(toFolder, fromFile.getName());
			if(!clearDestinationFolder && toFile.exists()) {
				if(overwrite) recursiveDelete(toFile);
				else continue;
			}
			if(!fromFile.renameTo(toFile)) throw new RuntimeException("Cannot rename " + fromFile.getAbsolutePath() + " to " + toFile.getAbsolutePath());
		}
	}
	
	public static String toString(File file) throws IOException {
		return toString(file, false, Charset.defaultCharset());
	}
	
	public static String toString(File file, Charset charset) throws IOException {
		return toString(file, false, charset);
	}
	
	public static String toString(File file, boolean gzip) throws IOException {
		return toString(file, gzip, Charset.defaultCharset());
	}
	
	public static String toString(File file, boolean gzip, Charset charset) throws IOException {
		InputStream is = new FileInputStream(file);
		if(gzip) is = new GZIPInputStream(is);
		return toString(is, charset);
	}
	
	public static String toString(InputStream is) throws IOException {
		return toString(is, Charset.defaultCharset());
	}
	
	public static String toString(InputStream is, Charset charset) throws IOException {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
			char[] buffer = new char[8192];
			StringBuilder sb = new StringBuilder();
			int read;
			while((read = reader.read(buffer, 0, buffer.length)) != -1) {
				sb.append(buffer, 0, read);
			}
			return sb.toString();
		}
		finally {
			is.close();
		}
	}
	
	public static void toFile(String str, File file) throws IOException {
		toFile(str, file, false, Charset.defaultCharset());
	}
	
	public static void toFile(String str, File file, Charset charset) throws IOException {
		toFile(str, file, false, charset);
	}
	
	public static void toFile(String str, File file, boolean gzip) throws IOException {
		toFile(str, file, gzip, Charset.defaultCharset());
	}
	
	public static void toFile(String str, File file, boolean gzip, Charset charset) throws IOException {
		OutputStream stream = new FileOutputStream(file);
		if(gzip) stream = new GZIPOutputStream(stream);
		toStream(str, stream, charset);
	}
	
	public static void toStream(String str, OutputStream stream) throws IOException {
		toStream(str, stream, Charset.defaultCharset());
	}
	
	public static void toStream(String str, OutputStream stream, Charset charset) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, charset));
		try {
			writer.write(str);
		}
		finally {
			writer.flush();
			writer.close();
		}
	}
	
	public static String dumpFolder(File folder) {
		if(!folder.isDirectory()) throw new IllegalArgumentException("folder must be a directory");
		StringBuilder sb = new StringBuilder();
		dumpFolderRecursive(folder, sb, 0);
		return sb.toString();
	}
	
	private static void dumpFolderRecursive(File folder, StringBuilder sb, int level) {
		for(int i=0; i < level; i++) sb.append("\t");
		sb.append(folder.getName());
		sb.append("\n");
		for(File file : folder.listFiles()) {
			if(file.isDirectory()) dumpFolderRecursive(file, sb, level + 1);
			else {
				for(int i=0; i <= level; i++) sb.append("\t");
				sb.append(file.getName());
				sb.append("\n");
			}
		}
	}
}
