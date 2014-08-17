/**
 * This file is part of DroidPHP
 *
 * (c) 2013 Shushant Kumar
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package net.pocketmine.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import android.content.Context;
import android.util.Log;

public final class ServerUtils {

	final static String TAG = "net.pocketmine.server.ServerUtils";
	static Context mContext;
	private static java.io.OutputStream stdin;
	private static java.io.InputStream stdout;

	final public static void setContext(Context mContext) {
		ServerUtils.mContext = mContext;

	}

	final public static String getAppDirectory() {

		return mContext.getApplicationInfo().dataDir;

	}

	final public static String getDataDirectory() {

		return android.os.Environment.getExternalStorageDirectory().getPath()
				+ "/PocketMine";

	}

	/**
	 * Instead of killing process by its PID you can use this method to kill
	 * process by specifying its name
	 * 
	 * @param mProcessName
	 *            Name Of Process that you want to kill
	 * @return boolean
	 */
	final public static Boolean killProcessByName(String mProcessName) {

		return execCommand(getAppDirectory() + "/killall " + mProcessName);
	}

	final public static void stopServer() {
		killProcessByName("php");
	}

	static Process serverProc;

	public static Boolean isRunning() {
		try {
			serverProc.exitValue();
		} catch (Exception e) {
			// do there the rest
			return true;
		}

		return false;
	}

	final public static void runServer() {
		File f = new File(getDataDirectory(), "tmp/");
		if (!f.exists()) {
			f.mkdir();
		} else if (!f.isDirectory()) {
			f.delete();
			f.mkdir();
		}
		setPermission();

		String file = "/PocketMine-MP.php";
		if (new File(getDataDirectory() + "/PocketMine-MP.phar").exists()) {
			file = "/PocketMine-MP.phar";
		}
		String[] serverCmd = { getAppDirectory() + "/php",
				// getAppDirectory() + "/php_data/PocketMine-MP.php"
				getDataDirectory() + file };

		ProcessBuilder builder = new ProcessBuilder(serverCmd);
		builder.redirectErrorStream(true);
		builder.directory(new File(getDataDirectory()));
		builder.environment().put("TMPDIR", getDataDirectory() + "/tmp");
		try {
			serverProc = builder.start();
			stdout = serverProc.getInputStream();
			stdin = serverProc.getOutputStream();

			LogActivity.log("[PocketMine] Server is starting...");

			Thread tMonitor = new Thread() {
				public void run() {
					InputStreamReader reader = new InputStreamReader(stdout,
							Charset.forName("UTF-8"));
					BufferedReader br = new BufferedReader(reader);
					LogActivity.log("[PocketMine] Server was started.");

					while (isRunning()) {
						try {
							char[] buffer = new char[8192];
							int size = 0;
							while ((size = br.read(buffer, 0, buffer.length)) != -1) {
								StringBuilder s = new StringBuilder();
								for (int i = 0; i < size; i++) {
									char c = buffer[i];
									if (c == '\r') {
									} //
									else if (c == '\n' || c == '\u0007') {
										String line = s.toString();
										Log.d(TAG, line);

										String lineNoDate = "";
										int iof = line.indexOf(" ");
										if (iof != -1)
											lineNoDate = line
													.substring(iof + 1);
										if (lineNoDate
												.startsWith("[CMD] There are ")
												&& requestPlayerRefresh
												&& requestPlayerRefreshCount == -1) {

											try {
												String num = lineNoDate
														.substring("[CMD] There are "
																.length());
												num = num.substring(0,
														num.indexOf("/"));
												requestPlayerRefreshCount = Integer
														.parseInt(num);

												if (requestPlayerRefreshCount == 0) {
													HomeActivity
															.updatePlayerList(null);
													requestPlayerRefresh = false;
												}
											} catch (Exception e) {
												e.printStackTrace();
											}
										} else if (lineNoDate
												.startsWith("[CMD] ")
												&& requestPlayerRefresh
												&& requestPlayerRefreshCount != -1) {

											String player = lineNoDate
													.substring(6);
											String[] players = player
													.split(", ");

											HomeActivity
													.updatePlayerList(players);

											requestPlayerRefresh = false;
										} else if (c == '\u0007'
												&& line.startsWith("\u001B]0;")) {
											line = line.substring(4);
											System.out
													.println("[Stat] " + line);
											HomeActivity.setStats(
													getStat(line, "Online"),
													getStat(line, "RAM"),
													getStat(line, "U"),
													getStat(line, "D"),
													getStat(line, "TPS"));
										} else {
											LogActivity.log("[Server] "
													+ (line.replace("&",
															"&amp;").replace(
															"<", "&lt;")
															.replace(">",
																	"&gt;")));

											if (line.contains("] logged in with entity id ")
													|| line.contains("] logged out due to ")) {
												refreshPlayers();
											}
										}

										s = new StringBuilder();
									} else {
										s.append(buffer[i]);
									}
								}

							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							try {
								br.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}

					LogActivity.log("[PocketMine] Server was stopped.");
					HomeActivity.stopNotifyService();
					HomeActivity.hideStats();
				}

			};
			tMonitor.start();

			Log.i(TAG, "PHP is started");
		} catch (java.lang.Exception e) {
			Log.e(TAG, "Unable to start PHP", e);
			LogActivity.log("[PocketMine] Unable to start PHP.");
			HomeActivity.stopNotifyService();
			HomeActivity.hideStats();
			killProcessByName("php");
		}

		return;

	}

	public static String getStat(String line, String stat) {
		stat = stat + " ";
		String result = line.substring(line.indexOf(stat) + stat.length());
		int iof = result.indexOf(" ");
		if (iof != -1) {
			result = result.substring(0, iof);
		}
		return result;
	}

	private static Boolean requestPlayerRefresh = false;
	private static int requestPlayerRefreshCount = -1;

	public static void refreshPlayers() {
		System.out.println("Refreshing player list");
		requestPlayerRefreshCount = -1;
		requestPlayerRefresh = true;
		executeCMD("list");
	}

	/**
	 * 
	 * @param mCommand
	 *            hold the command which will be executed by invoking {@link
	 *            Runtime.getRuntime.exec(...)}
	 * @return boolean
	 * @throws IOException
	 *             if it unable to execute the command
	 */

	final public static boolean execCommand(String mCommand) {

		/*
		 * Create a new Instance of Runtime
		 */
		Runtime r = Runtime.getRuntime();
		try {
			/**
			 * Executes the command
			 */
			r.exec(mCommand);

		} catch (java.io.IOException e) {

			Log.e(TAG, "execCommand", e);

			r = null;

			return false;
		}

		return true;

	}

	final static private void setPermission() {
		try {
			execCommand("/system/bin/chmod 777 " + getAppDirectory() + "/php");
			execCommand("/system/bin/chmod 777 " + getAppDirectory()
					+ "/killall");
		} catch (java.lang.Exception e) {
			Log.e(TAG, "setPermission", e);
		}

	}

	public static boolean checkIfInstalled() {

		File mPhp = new File(getAppDirectory() + "/php");
		File mPM = new File(getDataDirectory() + "/PocketMine-MP.php");
		File mPMPhar = new File(getDataDirectory() + "/PocketMine-MP.phar");

		int saveVer = HomeActivity.prefs != null ? HomeActivity.prefs.getInt(
				"filesVersion", 0) : 0;

		// File mMySql = new File(getAppDirectory() + "/mysqld");
		// File mLighttpd = new File(getAppDirectory() + "/lighttpd");
		// File mMySqlMon = new File(getAppDirectory() + "/mysql-monitor");

		if (mPhp.exists() && (mPM.exists() || mPMPhar.exists()) && saveVer == 4) {

			return true;

		}

		return false;

	}

	public static void executeCMD(String CCmd) {

		try {
			stdin.write((CCmd + "\r\n").getBytes());
			stdin.flush();
		} catch (Exception e) {
			// stdin.close();
			Log.e(TAG, "Cannot execute: " + CCmd, e);

		}
	}
}
