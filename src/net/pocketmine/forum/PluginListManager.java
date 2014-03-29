package net.pocketmine.forum;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.ArrayList;

import net.pocketmine.server.ServerUtils;

import android.util.Log;


public class PluginListManager {

	public static ArrayList<PluginDownloadInfo> plugins;

	public static class PluginDownloadInfo implements Serializable {
		private static final long serialVersionUID = 1L;

		public int id;
		public int updated;
		public String filename;
		public String hash; // warn user that changes will be erased
		public ArrayList<String> files; // when extracting from zip
	}

	public static void load() {
		File f = new File(ServerUtils.getDataDirectory() + "/plugins/.plugins");
		if (f.exists()) {
			try {
				FileInputStream fin = new FileInputStream(f);
				ObjectInputStream in = new ObjectInputStream(fin);
				plugins = (ArrayList<PluginDownloadInfo>) in.readObject();
				in.close();
				fin.close();
			} catch (Exception err) {
				Log.e("Plugins", "Failed to load plugins list.");
				err.printStackTrace();
				plugins = new ArrayList<PluginDownloadInfo>();
			}
		} else {
			plugins = new ArrayList<PluginDownloadInfo>();
		}
	}

	public static void save() {
		try {
			FileOutputStream fout = new FileOutputStream(
					ServerUtils.getDataDirectory() + "/plugins/.plugins");
			ObjectOutputStream out = new ObjectOutputStream(fout);
			out.writeObject(plugins);
			out.close();
			fout.close();
		} catch (Exception err) {
			Log.e("Plugins", "Failed to save plugins list.");
			err.printStackTrace();
		}
	}

	public static PluginDownloadInfo getPluginInfo(int pluginId) {
		if (plugins == null) {
			load();
		}

		for (PluginDownloadInfo plugin : plugins) {
			if (plugin.id == pluginId) {
				return plugin;
			}
		}
		return null;
	}

	public static boolean removePlugin(int pluginId) {
		if (plugins == null) {
			load();
		}

		for (PluginDownloadInfo plugin : plugins) {
			if (plugin.id == pluginId) {
				plugins.remove(plugin);
				save();
				return true;
			}
		}
		return false;
	}

	public static boolean removePlugin(PluginDownloadInfo plugin) {
		if (plugins == null) {
			load();
		}

		Boolean r = plugins.remove(plugin);
		if (r)
			save();
		return r;
	}

	public static void installPlugin(int pluginId, int updated,
			String filename, ArrayList<String> files) {
		if (plugins == null) {
			load();
		}
		
		PluginDownloadInfo temp = getPluginInfo(pluginId);
		if(temp != null)
			plugins.remove(temp);

		PluginDownloadInfo dlInfo = new PluginDownloadInfo();
		dlInfo.id = pluginId;
		dlInfo.updated = updated;
		dlInfo.filename = filename;
		dlInfo.hash = sha1(ServerUtils.getDataDirectory() + "/plugins/"
				+ filename);
		dlInfo.files = files;
		plugins.add(dlInfo);
		save();
	}

	// Utils
	private static String sha1(String file) {
		String hash = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream(file));
			int n = 0;
			byte[] buffer = new byte[1024];
			while ((n = bis.read(buffer)) != -1) {
				if (n > 0) {
					digest.update(buffer, 0, n);
				}
			}

			byte[] bytes = digest.digest();
			hash = bytesToHex(bytes);

			bis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hash;
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	private static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
