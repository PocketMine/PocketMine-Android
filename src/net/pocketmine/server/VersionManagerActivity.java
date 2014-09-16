package net.pocketmine.server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.pocketmine.server.R;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

public class VersionManagerActivity extends SherlockActivity {
	public ArrayAdapter<CharSequence> adapter;
	private Boolean install = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		install = getIntent().getBooleanExtra("install", false);
		setContentView(R.layout.version_manager);

		start();
	}

	public String getPageContext(String url) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(new URL(
				url).openStream()));
		StringBuilder sb = new StringBuilder();
		String str;
		while ((str = in.readLine()) != null) {
			sb.append(str);
		}
		in.close();
		return sb.toString();
	}

	private void start() {
		final ProgressBar pbar = (ProgressBar) findViewById(R.id.loadingBar);
		final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
		final Button skip = (Button) findViewById(R.id.skipBtn);
		skip.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		pbar.setVisibility(View.VISIBLE);
		scrollView.setVisibility(View.GONE);
		skip.setVisibility(View.GONE);

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					JSONObject softObj = (JSONObject) JSONValue
							.parse(getPageContext("http://pocketmine.net/api/?channel=soft"));
					final String softVersion = (String) softObj
							.get("version");
					final String softAPI = (String) softObj
							.get("api_version");
					Long date = (Long) softObj.get("date");
					Date d = new Date(date * 1000);
					final String softDate = SimpleDateFormat
							.getDateInstance().format(d);
					final String softDownloadURL = (String) softObj.get("download_url");
					
					JSONObject stableObj = (JSONObject) JSONValue
							.parse(getPageContext("http://pocketmine.net/api/?channel=stable"));
					final String stableVersion = (String) stableObj
							.get("version");
					final String stableAPI = (String) stableObj
							.get("api_version");
					date = (Long) stableObj.get("date");
					d = new Date(date * 1000);
					final String stableDate = SimpleDateFormat
							.getDateInstance().format(d);
					final String stableDownloadURL = (String) stableObj.get("download_url");

					JSONObject betaObj = (JSONObject) JSONValue
							.parse(getPageContext("http://pocketmine.net/api/?channel=beta"));
					final String betaVersion = (String) betaObj.get("version");
					final String betaAPI = (String) betaObj.get("api_version");
					date = (Long) betaObj.get("date");
					d = new Date(date * 1000);
					final String betaDate = SimpleDateFormat.getDateInstance()
							.format(d);
					final String betaDownloadURL = (String) betaObj.get("download_url");

					JSONObject devObj = (JSONObject) JSONValue
							.parse(getPageContext("http://pocketmine.net/api/?channel=development"));
					final String devVersion = (String) devObj.get("version");
					final String devAPI = (String) devObj.get("api_version");
					date = (Long) devObj.get("date");
					d = new Date(date * 1000);
					final String devDate = SimpleDateFormat.getDateInstance()
							.format(d);
					final String devDownloadURL = (String) devObj.get("download_url");
					
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							TextView softVersionView = (TextView) findViewById(R.id.soft_version);
							TextView softDateView = (TextView) findViewById(R.id.soft_date);
							Button softDownload = (Button) findViewById(R.id.download_soft);
							TextView stableVersionView = (TextView) findViewById(R.id.stable_version);
							TextView stableDateView = (TextView) findViewById(R.id.stable_date);
							Button stableDownload = (Button) findViewById(R.id.download_stable);
							TextView betaVersionView = (TextView) findViewById(R.id.beta_version);
							TextView betaDateView = (TextView) findViewById(R.id.beta_date);
							Button betaDownload = (Button) findViewById(R.id.download_beta);
							TextView devVersionView = (TextView) findViewById(R.id.dev_version);
							TextView devDateView = (TextView) findViewById(R.id.dev_date);
							Button devDownload = (Button) findViewById(R.id.download_dev);

							softVersionView.setText("Version: "
									+ softVersion + " (API: " + softAPI
									+ ")");
							softDateView.setText(softDate);
							softDownload.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									download(softDownloadURL, softVersion);
								}
							});
							
							stableVersionView.setText("Version: "
									+ stableVersion + " (API: " + stableAPI
									+ ")");
							stableDateView.setText(stableDate);
							stableDownload.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									download(stableDownloadURL, stableVersion);
								}
							});

							betaVersionView.setText("Version: " + betaVersion
									+ " (API: " + betaAPI + ")");
							betaDateView.setText(betaDate);
							betaDownload.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									download(betaDownloadURL, betaVersion);
								}
							});

							devVersionView.setText("Version: " + devVersion
									+ " (API: " + devAPI + ")");
							devDateView.setText(devDate);
							devDownload.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									download(devDownloadURL, devVersion);
								}
							});

							pbar.setVisibility(View.GONE);
							scrollView.setVisibility(View.VISIBLE);
							if (install) {
								skip.setVisibility(ServerUtils
										.checkIfInstalled() ? View.VISIBLE
										: View.GONE);
							}
						}
					});
				} catch (Exception err) {
					err.printStackTrace();
					if (install) {
						showToast("Cannot load version list. Retrying in 5 seconds...");
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									finish();
								}
							});
							return;
						}
						start();
					} else {
						showToast("Cannot load version list.");
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								finish();
							}
						});
					}
				}
			}
		}).start();
	}

	private void download(final String address, final String fver) {
		File vdir = new File(ServerUtils.getDataDirectory()
											+ "/versions/");
		if(!vdir.exists()){
			vdir.mkdirs();
		}
		
		final VersionManagerActivity ctx = this;
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				final ProgressDialog dlDialog = new ProgressDialog(ctx);
				dlDialog.setMax(100);
				dlDialog.setTitle("Downloading this version...");
				dlDialog.setMessage("Please wait...");
				dlDialog.setIndeterminate(false);
				dlDialog.setCancelable(false);
				dlDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				dlDialog.show();
				dlDialog.setProgress(0);
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							URL url = new URL(address);
							URLConnection connection = url.openConnection();
							connection.connect();
							int fileLength = connection.getContentLength();

							InputStream input = new BufferedInputStream(url
									.openStream());
							OutputStream output = new FileOutputStream(
									ServerUtils.getDataDirectory()
											+ "/versions/" + fver + ".phar");

							byte data[] = new byte[1024];
							long total = 0;
							int count;
							int lastProgress = 0;
							while ((count = input.read(data)) != -1) {
								total += count;
								int progress = (int) (total * 100 / fileLength);
								if (progress != lastProgress) {
									dlDialog.setProgress(progress);
									lastProgress = progress;
								}
								output.write(data, 0, count);
							}

							output.flush();
							output.close();
							input.close();
						} catch (Exception e) {
							e.printStackTrace();
							showToast("Failed to download this version.");
							dlDialog.dismiss();
							return;
						}

						dlDialog.dismiss();

						install(fver);
						// dlDialog.setTitle("Installing this version...");
						// dlDialog.show();
					}

				}).start();
			}
		});
	}

	private void install(CharSequence ver) {
		final VersionManagerActivity ctx = this;
		final CharSequence fver = ver;

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				final ProgressDialog iDialog = new ProgressDialog(ctx);
				iDialog.setMax(100);
				iDialog.setTitle("Installing this version...");
				iDialog.setMessage("Please wait...");
				iDialog.setIndeterminate(false);
				iDialog.setCancelable(false);
				iDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				iDialog.show();
				iDialog.setProgress(0);

				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							new File(ServerUtils.getDataDirectory()
									+ "/PocketMine-MP.php").delete();
						} catch (Exception e) {
						}
						try {
							delete(new File(ServerUtils.getDataDirectory()
									+ "/src/"));
						} catch (Exception e) {
						}
						try {
							new File(ServerUtils.getDataDirectory()
									+ "/PocketMine-MP.phar").delete();
						} catch (Exception e) {
						}

						try {
							FileInputStream in = new FileInputStream(
									ServerUtils.getDataDirectory()
											+ "/versions/" + fver + ".phar");

							FileOutputStream out = new FileOutputStream(
									ServerUtils.getDataDirectory()
											+ "/PocketMine-MP.phar");
							byte[] buffer = new byte[1024];
							int len;
							while ((len = in.read(buffer)) > 0) {
								out.write(buffer, 0, len);
							}
							in.close();
							out.close();

							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									if (install) {
										Intent ver = new Intent(
												VersionManagerActivity.this,
												ConfigActivity.class);
										ver.putExtra("install", true);
										startActivity(ver);
									}

									ctx.finish();
								}
							});
						} catch (Exception e) {
							showToast("Failed to install this version.");
							e.printStackTrace();
						}
					}
				}).start();
			}
		});

	}

	public void delete(File f) {
		if (f.isDirectory()) {
			File[] files = f.listFiles();
			for (File file : files) {
				delete(file);
			}
		}
		f.delete();
	}

	public void showToast(String msg) {
		final String fmsg = msg;
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), fmsg,
						Toast.LENGTH_SHORT).show();
			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && install) {

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}