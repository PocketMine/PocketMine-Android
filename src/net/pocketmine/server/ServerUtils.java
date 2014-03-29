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
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

public final class ServerUtils {

	final static String TAG = "com.MrARM.DroidPocketMine.ServerUtils";
	static Context mContext;
	private static java.io.OutputStream stdin;
	private static java.io.InputStream stdout;

	// private static String serverPort;
	// private static String httpdUri;

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

	/**
	 * This method is responsible for invoking
	 * {@link ServerUtils.killProcessByName} which kills all the running
	 * instances of <strong>PHP</strong>, <strong>LIGHTTPD</strong>,
	 * <strong>MYSQLD</strong>, <strong>MYSQL Monitor </strong>
	 * 
	 * @return
	 */
	/*
	 * final public static void setServerPort(String port) { serverPort = port;
	 * 
	 * }
	 * 
	 * final public static void setHttpDocsUri(String Uri) {
	 * 
	 * httpdUri = Uri;
	 * 
	 * }
	 */

	final public static void stopServer() {

		/**
		 * 
		 * Kill the Running Instances of all called process name. PHP is
		 * automatically kill when instance <strong>LIGHTTPD</strong> is
		 * destroyed. Anyway if it is unable to kill <strong>PHP</strong> lets
		 * kill by invoking <b>killall</b> command
		 */
		// killProcessByName("lighttpd");
		/**
		 * see above doc why i called PHP here
		 */
		killProcessByName("php");
		// killProcessByName("mysqld");
		// killProcessByName("mysql-monitor");

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
		// restoreOrCreateServerData();
		// restoreConfiguration("lighttpd.conf");
		// restoreConfiguration("php.ini");
		// restoreConfiguration("mysql.ini");
		setPermission();

		String[] serverCmd = { getAppDirectory() + "/php",
				// getAppDirectory() + "/php_data/PocketMine-MP.php"
				getDataDirectory() + "/PocketMine-MP.php" };

		try {
			serverProc = (new ProcessBuilder(serverCmd)).redirectErrorStream(
					true).start();
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
			// execCommand("/system/bin/chmod 755 " + getAppDirectory() +
			// "/tmp");

		} catch (java.lang.Exception e) {
			Log.e(TAG, "setPermission", e);
		}

	}

	/*
	 * final private static void restoreConfiguration(String fileName) {
	 * 
	 * File isConf = new File(getHttpDirectory() + "/conf/" + fileName); if
	 * (!isConf.exists()) {
	 * 
	 * try {
	 * 
	 * String mString;
	 * 
	 * java.io.InputStream mStream = mContext.getAssets().open( fileName,
	 * AssetManager.ACCESS_BUFFER);
	 * 
	 * java.io.BufferedWriter outputStream = new java.io.BufferedWriter( new
	 * java.io.FileWriter(getHttpDirectory() + "/tmp/" + fileName));
	 * 
	 * int c; while ((c = mStream.read()) != -1) { outputStream.write(c); }
	 * outputStream.close(); mStream.close();
	 * 
	 * mString = org.apache.commons.io.FileUtils.readFileToString( new
	 * File(getHttpDirectory() + "/tmp/" + fileName), "UTF-8");
	 * 
	 * mString = mString.replace("%app_dir%", getAppDirectory()); mString =
	 * mString.replace("%http_dir%", getHttpDirectory()); mString =
	 * mString.replace("%port%", serverPort);
	 * org.apache.commons.io.FileUtils.writeStringToFile(new File(
	 * getHttpDirectory() + "/conf/" + fileName), mString, "UTF-8"); } catch
	 * (java.lang.Exception e) { Log.e(TAG, "Unable to copy " + fileName +
	 * " from assets", e);
	 * 
	 * } }
	 * 
	 * }
	 */

	/*
	 * final private static void restoreOrCreateServerData() {
	 * 
	 * File mFile = new File(getHttpDirectory() + "/conf/"); if
	 * (!mFile.exists()) mFile.mkdirs();
	 * 
	 * mFile = new File(getHttpDirectory() + "/php_data/");
	 * 
	 * if (!mFile.exists()) mFile.mkdir();
	 * 
	 * mFile = new File(getHttpDirectory() + "/logs/");
	 * 
	 * if (!mFile.exists()) mFile.mkdir(); mFile = new File(getHttpDirectory() +
	 * "/tmp/");
	 * 
	 * if (!mFile.exists()) mFile.mkdir();
	 * 
	 * mFile = null;
	 * 
	 * }
	 */

	public static boolean checkIfInstalled() {

		File mPhp = new File(getAppDirectory() + "/php");
		File mPM = new File(getDataDirectory() + "/PocketMine-MP.php");

		int saveVer = HomeActivity.prefs != null ? HomeActivity.prefs.getInt(
				"filesVersion", 0) : 0;

		// File mMySql = new File(getAppDirectory() + "/mysqld");
		// File mLighttpd = new File(getAppDirectory() + "/lighttpd");
		// File mMySqlMon = new File(getAppDirectory() + "/mysql-monitor");

		if (mPhp.exists() && mPM.exists() && saveVer == 3) {

			return true;

		}

		return false;

	}

	/**
	 * StrictMode.ThreadPolicy was introduced science API level 9 and its
	 * default implementation has been change since API Level 11 and do not
	 * allow network operation on UI Thread and lots more reason Hence, its
	 * better to perform These Stuff outside UI Thread you may do it in
	 * <strong>AsyncTask</strong> And this method allow to permit all
	 * restriction made by system itself.
	 */
	@android.annotation.TargetApi(android.os.Build.VERSION_CODES.GINGERBREAD)
	public static void StrictModePermitAll() {
		try {
			android.os.StrictMode
					.setThreadPolicy((new android.os.StrictMode.ThreadPolicy.Builder())
							.permitAll().build());
		} catch (Exception e) {
			e.printStackTrace();
			// oops!
		}
	}

	/**
	 * 
	 * @param mUsername
	 * @param mPassword
	 */

	/*
	 * public static void startMYSQLMointor(String mUsername, String mPassword)
	 * { String[] query = new String[] { getAppDirectory() + "/mysql-monitor",
	 * "-h", "127.0.0.1", "-T", "-f", "-r", "-t", "-E", "--disable-pager", "-n",
	 * "--user=" + mUsername, "--password=" + mPassword,
	 * "--default-character-set=utf8", "-L" }; try {
	 * 
	 * ProcessBuilder pb = (new ProcessBuilder(query));
	 * pb.redirectErrorStream(true); proc = pb.start(); stdin =
	 * proc.getOutputStream(); stdout = proc.getInputStream();
	 * 
	 * } catch (IOException e) {
	 * 
	 * Log.e(TAG, "MSQL Monitor", e); /** I have commented
	 * <string>proc.destroy</strong> because this is usually cause bug
	 *//*
		 * // proc.destroy(); }
		 * 
		 * }
		 */

	public static void executeCMD(String CCmd) {

		try {
			/**
			 * \r\n lets the code to be executed and getBytes converts chars in
			 * bytes Array
			 */
			stdin.write((CCmd + "\r\n").getBytes());
			stdin.flush();
		} catch (Exception e) {
			// stdin.close();
			Log.e(TAG, "ERROR on Executing: " + CCmd, e);

		}
		// return readExecutedSQLShellCMD();

	}

	/**
	 * This Method is not in use any more because its causing bug after end of
	 * iteration
	 * 
	 * @see mySQLShell().ShellAsync()
	 * @return
	 * @throws IOException
	 */
	/*
	 * public static String readExecutedSQLShellCMD() throws IOException { /*
	 * Returns an input stream that is connected to the standard output stream
	 * (stdout) of the native process represented by this object
	 *//*
		 * java.io.BufferedReader buffr = new java.io.BufferedReader( new
		 * java.io.InputStreamReader(stdout)); String sb = new String(); try {
		 * while (true) { String READ = buffr.readLine();
		 * 
		 * if (READ == null) { break; } sb += READ + "\n";
		 * 
		 * }
		 * 
		 * Log.e(TAG, "Result = " + sb.toString()); } catch (IOException e) {
		 * 
		 * stdout.close(); Log.e(TAG, "Unable to read mysql stream", e);
		 * 
		 * }
		 * 
		 * return sb.toString();
		 * 
		 * }
		 */
}
