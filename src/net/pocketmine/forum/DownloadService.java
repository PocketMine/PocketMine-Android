package net.pocketmine.forum;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.pocketmine.forum.PluginListManager.PluginDownloadInfo;
import net.pocketmine.server.ServerUtils;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class DownloadService extends Service {
	private static class QueueDownload {
		public int id;
		public String url;
		public String path;
		public String filename;
		public int updated;

		public QueueDownload(int id, String url, String path, String filename,
				int updated) {
			this.id = id;
			this.url = url;
			this.path = path;
			this.filename = filename;
			this.updated = updated;
		}
	}

	private Handler handler = new Handler();

	private static ArrayList<QueueDownload> downloads = new ArrayList<QueueDownload>();

	private static Boolean downloading = false;
	private static int cid = -1;
	private static int progress = -1;
	private static Thread t;
	private static URLConnection connection;
	private static BufferedInputStream input;

	private static Boolean stop = false;

	public static DownloadService runningService = null;

	public static final String RECEIVER = "net.pocketmine.forum.download.receiver";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		runningService = this;

		final int id = intent.getIntExtra("id", -1);
		Log.d("DownloadService", "ID:" + id);
		if (intent.getBooleanExtra("stop", false)) {
			if (id == cid) {
				Log.d("DownloadService", "Stop current");
				try {
					((HttpURLConnection) connection).disconnect();
					stop = true;
					input.close();
					Log.d("DownloadService", "Stopping");
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (downloads.size() <= 0) {
					stopSelf();
				}
				return START_NOT_STICKY;
			}
			Log.d("DownloadService", "Stop other");
			for (int i = 0; i < downloads.size(); i++) {
				if (downloads.get(i).id == id) {
					downloads.remove(i);
				}
			}
			return START_NOT_STICKY;
		}
		final String url = intent.getStringExtra("url");
		if (url == null) {
			Log.e("DownloadService", "Stopping: No URL");
			return START_NOT_STICKY;
		}
		final String path = intent.getStringExtra("path");
		if (path == null) {
			Log.e("DownloadService", "Stopping: No path");
			return START_NOT_STICKY;
		}
		final int updated = intent.getIntExtra("updated", -1);
		final String filename = intent.getStringExtra("filename");

		if (downloading) {
			downloads.add(new QueueDownload(id, url, path, filename, updated));
		} else {
			t = new Thread(new Runnable() {

				@Override
				public void run() {
					download(id, url, path, filename, updated);
				}
			});
			t.start();
		}

		return START_NOT_STICKY;
	}

	private void download(int id, String url, String path, String filename,
			int updated) {
		downloading = true;
		stop = false;
		Log.d("DownloadService", "Starting...");
		cid = id;
		progress = -1;

		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		try {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					this);
			builder.setContentTitle("Plugin download")
					.setContentText("Downloading...")
					.setSmallIcon(android.R.drawable.stat_sys_download)
					.setOngoing(true);
			builder.setProgress(0, 0, true);
			startForeground(2, builder.build());
			manager.notify(2, builder.build());

			URL mURL = new URL(url);
			connection = mURL.openConnection();
			int fileLength = connection.getContentLength();

			if (!stop) {

				input = new BufferedInputStream(mURL.openStream());
				OutputStream output = new FileOutputStream(path + ".download");

				byte data[] = new byte[1024];
				long total = 0;
				int count;
				int lastProgress = 0;

				Intent iStart = new Intent(RECEIVER);
				iStart.putExtra("id", id);
				iStart.putExtra("type", 0);
				sendBroadcast(iStart);
				Log.d("DownloadService", "Started.");

				while ((count = input.read(data)) != -1) {
					total += count;
					int progress = (int) (total * 100 / fileLength);
					if (progress != lastProgress) {
						this.progress = progress;
						Log.d("DownloadService", "Progress: " + progress);
						builder.setProgress(100, progress, false)
								.setContentText(progress + "%");
						manager.notify(2, builder.build());
						Intent iProgress = new Intent(RECEIVER);
						iProgress.putExtra("id", id);
						iProgress.putExtra("type", 1);
						iProgress.putExtra("progress", progress);
						sendBroadcast(iProgress);

						lastProgress = progress;
					}
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();

				
				PluginDownloadInfo info = PluginListManager.getPluginInfo(id);
				if(info != null){
					// we are updating!
					// first of all, deinstall the plugin

					ArrayList<String> files = info.files;
					if (files != null) {
						for (int i = 0; i < files.size(); i++) {
							File f = new File(ServerUtils.getDataDirectory()
									+ "/plugins/" + files.get(i));
							if (f.exists())
								f.delete();
						}
					}

					// Deinstall from db - in case everything fails - user doesn't have a broken entry!
					PluginListManager.removePlugin(info);
				}
				
				int iof = path.indexOf(".");
				Boolean installAsIs = true;
				ArrayList<String> files = null;
				if (iof != -1) {
					String ext = path.substring(iof);
					if (ext.equals(".php") || ext.equals(".pmf") || ext.equals(".phar")) {
						installAsIs = true;
					} else if (ext.equals(".zip")) {
						installAsIs = false;
						// it will extract all .pmf/.php into plugins/

						files = new ArrayList<String>();
						filename += ".download";
						String zipFileName = path + ".download";
						// ZipFile zip = new ZipFile(zipFileName);
						FileInputStream fin = new FileInputStream(zipFileName);
						ZipInputStream zin = new ZipInputStream(fin);
						ZipEntry ze = null;
						String loc = ServerUtils.getDataDirectory()
								+ "/plugins/";
						while ((ze = zin.getNextEntry()) != null) {
							if (!ze.isDirectory()) {
								String zeName = ze.getName();
								int extDot = zeName.lastIndexOf(".");
								Boolean extract = false;
								if (extDot != -1) {
									String extresion = zeName
											.substring(extDot + 1);
									if (extresion.equals("php")
											|| extresion.equals("pmf")
											|| extresion.equals("phar"))
										extract = true;
								}
								if (extract) {
									int firstSlash = zeName.lastIndexOf("/");
									if (firstSlash != -1) {
										zeName = zeName
												.substring(firstSlash + 1);
									}

									java.io.File f = new java.io.File(loc
											+ zeName);
									f = new java.io.File(f.getParent());
									if (!f.isDirectory())
										f.mkdirs();

									FileOutputStream fout = new FileOutputStream(
											loc + zeName);
									byte[] buffer = new byte[1024 * 2 * 2];
									int b = -1;
									while ((b = zin.read(buffer)) != -1) {
										fout.write(buffer, 0, b);
									}

									fout.close();

									files.add(zeName);
								}
								zin.closeEntry();
							}
						}
						zin.close();
						// zip.close();
					} else if (ext.equals(".gz") || ext.equals(".tar")) {

						if (ext.equals(".tar")) {
							installAsIs = false;
						} else {
							String withoutExt = path.substring(0, iof);
							iof = withoutExt.lastIndexOf(".");
							if (iof != -1) {
								String ext2 = withoutExt.substring(iof);
								if (ext2.equals(".tar")) {
									installAsIs = false;
								}
							}
						}

						if (!installAsIs) {
							files = new ArrayList<String>();
							filename += ".download";

							String zipFileName = path + ".download";
							FileInputStream fin = new FileInputStream(
									zipFileName);
							ArchiveInputStream in = null;
							if (ext.equals(".gz")) {
								in = new TarArchiveInputStream(
										new GzipCompressorInputStream(
												new BufferedInputStream(fin)));
							} else {
								in = new TarArchiveInputStream(
										new BufferedInputStream(fin));
							}

							String loc = ServerUtils.getDataDirectory()
									+ "/plugins/";

							ArchiveEntry ze;
							while ((ze = in.getNextEntry()) != null) {
								if (!ze.isDirectory()) {
									String zeName = ze.getName();
									int extDot = zeName.lastIndexOf(".");
									Boolean extract = false;
									if (extDot != -1) {
										String extresion = zeName
												.substring(extDot + 1);
										if (extresion.equals("php")
												|| extresion.equals("pmf")
												|| extresion.equals("phar"))
											extract = true;
									}
									if (extract) {
										int firstSlash = zeName
												.lastIndexOf("/");
										if (firstSlash != -1) {
											zeName = zeName
													.substring(firstSlash + 1);
										}

										java.io.File f = new java.io.File(loc
												+ zeName);
										f = new java.io.File(f.getParent());
										if (!f.isDirectory())
											f.mkdirs();

										FileOutputStream fout = new FileOutputStream(
												loc + zeName);
										byte[] buffer = new byte[1024 * 2 * 2];
										int b = -1;
										while ((b = in.read(buffer)) != -1) {
											fout.write(buffer, 0, b);
										}

										fout.close();

										files.add(zeName);
									}
								}
							}

							in.close();

							files = new ArrayList<String>();
							filename += ".download";

						}
					}
				}

				if (installAsIs) {

					File f = new File(path);
					if (f.exists()) {
						f.delete();
					}
					new File(path + ".download").renameTo(f);
				}

				PluginListManager.installPlugin(id, updated, filename, files);

				Log.d("DownloadService", "Done.");

				Intent iEnd = new Intent(RECEIVER);
				iEnd.putExtra("id", id);
				iEnd.putExtra("type", 2);
				iEnd.putExtra("filename", path);
				sendBroadcast(iEnd);
			} else {
				File f = new File(path);
				if (f.exists()) {
					f.delete();
				}
				Intent iErr = new Intent(RECEIVER);
				iErr.putExtra("id", id);
				iErr.putExtra("type", 4);
				sendBroadcast(iErr);
			}
			manager.cancel(2);

		} catch (Exception e) {

			manager.cancel(2);

			if (stop) {
				File f = new File(path);
				if (f.exists()) {
					f.delete();
				}
				Intent iErr = new Intent(RECEIVER);
				iErr.putExtra("id", id);
				iErr.putExtra("type", 4);
				sendBroadcast(iErr);
			} else {
				Intent iErr = new Intent(RECEIVER);
				iErr.putExtra("id", id);
				iErr.putExtra("type", 3);
				sendBroadcast(iErr);

				handler.post(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(DownloadService.this,
								"Failed to download the plugin.",
								Toast.LENGTH_SHORT).show();
					}
				});

				e.printStackTrace();
			}
		}

		cid = -1;
		downloading = false;

		if (downloads.size() > 0) {
			QueueDownload dl = downloads.get(0);
			downloads.remove(0);
			download(dl.id, dl.url, dl.path, dl.filename, dl.updated);
		} else {
			stopSelf();
		}
	}

	/*
	 * Returns: 0-100: progress -1: not started -2: not on the list
	 */
	public int getProgress(int id) {
		if (id == cid) {
			return progress;
		} else {
			for (int i = 0; i < downloads.size(); i++) {
				if (downloads.get(i).id == id) {
					return -1;
				}
			}
		}

		return -2;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
