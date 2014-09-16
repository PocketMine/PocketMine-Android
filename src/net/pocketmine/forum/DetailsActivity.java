package net.pocketmine.forum;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.oelerich.BBCodeParser.SimpleParser;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import net.pocketmine.forum.PluginListManager.PluginDownloadInfo;
import net.pocketmine.server.HomeActivity;
import net.pocketmine.server.R;
import net.pocketmine.server.ServerUtils;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DetailsActivity extends SherlockActivity {

	public static String ClickFixScript = "<script>"
			+ "var arr = document.getElementsByTagName('a');"
			+ "for(var i=0;i<arr.length;i++){"
			+ "arr[i].addEventListener('touchstart', handle, false);" + "}"
			+ "function handle(){" + "window.location = this.href;" + "}"
			+ "</script>";

	public ProgressBar _loader;
	public LinearLayout _details;

	public int pluginID;
	public PluginsActivity.Plugin plugin;
	public long versionId;
	Boolean isInstalled;
	Boolean downloading = false;

	public ImageView _icon;
	public TextView _name;
	public TextView _author;
	public RatingBar _rate;
	public RatingBar _rate2;
	public TextView _rate2value;
	public TextView _rateCount;
	public TextView _rate2Count;
	public TextView _lastUpdate;
	public TextView _downloads;
	public TextView _downloadSize;
	public TextView _description;
	public WebView _descriptionHTML;
	public ImageView _descExpand;
	public LinearLayout _reviews;
	public LinearLayout _links;
	public RelativeLayout _progressLayout;
	public TextView _progressValue;
	public ProgressBar _progress;
	public Button _download;
	public Button _update;

	public Boolean customDl;
	public String dlURL;
	public int fileSize;
	public String fileSizeText;
	public String fileExt;
	public String extURL;
	public String supportURL;
	public ArrayList<Review> reviews;

	public String tagLine;
	public String description;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plugin_details);

		pluginID = getIntent().getIntExtra("id", 0);
		if (PluginsActivity.plugins != null) {
			for (PluginsActivity.Plugin p : PluginsActivity.plugins) {
				if (p.id == pluginID) {
					plugin = p;
				}
			}
		}

		ActionBar bar = getSupportActionBar();
		bar.setHomeButtonEnabled(true);
		bar.setDisplayHomeAsUpEnabled(true);
		Drawable colorDrawable = new ColorDrawable(PluginsActivity.color);
		Drawable bottomDrawable = getResources().getDrawable(
				R.drawable.actionbar_bottom);
		LayerDrawable ld = new LayerDrawable(new Drawable[] { colorDrawable,
				bottomDrawable });
		bar.setBackgroundDrawable(ld);

		_loader = (ProgressBar) findViewById(R.id.plugin_loading);
		_details = (LinearLayout) findViewById(R.id.plugin_info);

		_icon = (ImageView) findViewById(R.id.plugin_icon);
		_name = (TextView) findViewById(R.id.plugin_name);
		_author = (TextView) findViewById(R.id.plugin_author);
		_rate = (RatingBar) findViewById(R.id.plugin_rate);
		_rate2 = (RatingBar) findViewById(R.id.plugin_rate2);
		_rate2value = (TextView) findViewById(R.id.plugin_rate2_value);
		_rateCount = (TextView) findViewById(R.id.plugin_rate_count);
		_rate2Count = (TextView) findViewById(R.id.plugin_rate2_count);
		_lastUpdate = (TextView) findViewById(R.id.plugin_updated);
		_downloads = (TextView) findViewById(R.id.plugin_downloads);
		_downloadSize = (TextView) findViewById(R.id.plugin_size);
		_description = (TextView) findViewById(R.id.plugin_description);
		_descriptionHTML = (WebView) findViewById(R.id.plugin_description_html);
		_descExpand = (ImageView) findViewById(R.id.plugin_desc_expand);
		_reviews = (LinearLayout) findViewById(R.id.plugin_reviews);
		_links = (LinearLayout) findViewById(R.id.plugin_links);
		_progressLayout = (RelativeLayout) findViewById(R.id.plugin_progress_layout);
		_progress = (ProgressBar) findViewById(R.id.plugin_progress);
		_progressValue = (TextView) findViewById(R.id.plugin_progress_info);
		_download = (Button) findViewById(R.id.plugin_download);
		_update = (Button) findViewById(R.id.plugin_update);

		if (plugin == null) {
			RelativeLayout rl = new RelativeLayout(this);
			RelativeLayout.LayoutParams pb_params = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			pb_params.addRule(RelativeLayout.CENTER_IN_PARENT);
			ProgressBar pb = new ProgressBar(this);
			pb.setLayoutParams(pb_params);
			rl.addView(pb);
			setContentView(rl);
		} else {
			if (plugin.image != null) {
				_icon.setImageBitmap(plugin.image);
			} else {
				new GridAdapter.DownloadImageTask(_icon).execute(plugin);
			}
			_name.setText(plugin.name);
			_author.setText(plugin.author);
			_rate.setRating(Double.valueOf(plugin.rated).floatValue());
			_rateCount.setText("" + plugin.ratedTimes);
			_downloads.setText("Downloads: " + plugin.downloadCount);
			_downloadSize.setText("");
			Date d = new Date(
					Integer.valueOf(plugin.lastUpdate).longValue() * 1000);
			_lastUpdate.setText(new SimpleDateFormat("d MMM yyyy").format(d));
		}

		WebSettings webSettings = _descriptionHTML.getSettings();
		webSettings.setJavaScriptEnabled(true);
		_descriptionHTML.setOnTouchListener(new OnTouchListener() {

			boolean moved = false;
			MotionEvent originalEvent;

			@Override
			public boolean onTouch(final View view, MotionEvent event) {

				_descriptionHTML.onTouchEvent(event);

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					moved = false;
					originalEvent = event;
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					if (originalEvent != null
							&& event.getX() == originalEvent.getX()
							&& event.getY() == originalEvent.getY()) {
						moved = true;
					}
				} else if (event.getAction() == MotionEvent.ACTION_UP
						&& !moved
						&& originalEvent != null
						&& event.getEventTime() - originalEvent.getEventTime() < 500) {
					new Timer().schedule(new TimerTask() {

						@Override
						public void run() {
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									expandDesc(view);
								}
							});
						}
					}, 50);
				}
				return true;
			}
		});
		_descriptionHTML.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				descExpandCancel = true;
				view.getContext().startActivity(
						new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				return true;
			}
		});

		Button allReviews = (Button) findViewById(R.id.more_reviews);
		allReviews.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				Intent intent = new Intent(DetailsActivity.this,
						ReviewActivity.class);
				intent.putExtra("pluginID", pluginID);
				intent.putExtra("reviews", reviews);
				startActivity(intent);
			}
		});

		_download.setVisibility(View.GONE);
		_update.setVisibility(View.GONE);

		if (DownloadService.runningService != null
				&& DownloadService.runningService.getProgress(pluginID) != -2) {
			// _download.setVisibility(View.GONE);
			downloading = true;
			_progressLayout.setVisibility(View.VISIBLE);
			int progress = DownloadService.runningService.getProgress(pluginID);
			if (progress == -1) {
				_progressValue.setText("Downloading...");
				_progress.setIndeterminate(true);
			} else {
				_progress.setIndeterminate(false);
				_progressValue.setText(progress + "%");
				_progress.setProgress(progress);
			}
		}

		_download.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// DOWNLOAD/DEINSTALL
				// 1. CHECK SERVER RUNNING
				if (HomeActivity.isStarted) {
					Toast.makeText(DetailsActivity.this,
							"To download a plugin, stop the server first.",
							Toast.LENGTH_SHORT).show();
					return;
				}

				if (!isInstalled) {
					download();
				} else {
					// 2. GET INFO (AND FILENAME)
					PluginDownloadInfo info = PluginListManager
							.getPluginInfo(pluginID);
					if (info != null) {
						// 3. REMOVE FILES
						File f = new File(ServerUtils.getDataDirectory()
								+ "/plugins/" + info.filename);
						if (f.exists())
							f.delete();

						ArrayList<String> files = info.files;
						if (files != null) {
							for (int i = 0; i < files.size(); i++) {
								f = new File(ServerUtils.getDataDirectory()
										+ "/plugins/" + files.get(i));
								if (f.exists())
									f.delete();
							}
						}

						// 4. REMOVE FROM DATABASE
						PluginListManager.removePlugin(info);

						updateDownloadButton();
					}
				}
			}
		});

		_update.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (HomeActivity.isStarted) {
					Toast.makeText(DetailsActivity.this,
							"To download a plugin, stop the server first.",
							Toast.LENGTH_SHORT).show();
					return;
				}

				download();
			}
		});

		ImageView downloadCancel = (ImageView) findViewById(R.id.plugin_download_cancel);
		downloadCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				Intent i = new Intent(DetailsActivity.this,
						DownloadService.class);
				i.putExtra("id", pluginID);
				i.putExtra("stop", true);
				startService(i);
			}
		});

		registerReceiver(receiver, new IntentFilter(DownloadService.RECEIVER));

		refresh();
	}

	public void download() {
		// 1. IF IT'S EXTERNAL OPEN THE LINK IN BROWSER
		if (customDl) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(dlURL)));
			return;
		}
		// 2. FINALLY, WE CAN DOWNLOAD THE PLUGIN OWN WAY
		downloading = true;
		_download.setVisibility(View.GONE);
		_update.setVisibility(View.GONE);
		_progressLayout.setVisibility(View.VISIBLE);
		_progressValue.setText("Downloading...");
		_progress.setIndeterminate(true);
		Intent i = new Intent(DetailsActivity.this, DownloadService.class);
		i.putExtra("id", pluginID);
		i.putExtra("url", "http://forums.pocketmine.net/plugins/" + pluginID
				+ "/download?version=" + versionId);
		i.putExtra("path", ServerUtils.getDataDirectory() + "/plugins/plugin-"
				+ pluginID + fileExt);
		i.putExtra("filename", "plugin-" + pluginID + fileExt);
		i.putExtra("updated", plugin.lastUpdate);
		startService(i);
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context ctx, Intent i) {
			int id = i.getIntExtra("id", -1);
			if (id == pluginID) {
				// It belongs to us
				int type = i.getIntExtra("type", -1);
				if (type == 0) {
					// We have started downloading!
					_progress.setIndeterminate(false);
					_progress.setProgress(0);
					_progressValue.setText("0%");
				} else if (type == 1) {
					int progress = i.getIntExtra("progress", 0);
					_progress.setProgress(progress);
					_progressValue.setText(progress + "%");
				} else if (type == 2) {
					// Succed!
					_progressLayout.setVisibility(View.GONE);
					_download.setVisibility(View.VISIBLE);
					updateDownloadButton();
					downloading = false;
				} else if (type == 3) {
					// Failed :(
					_progressLayout.setVisibility(View.GONE);
					_download.setVisibility(View.VISIBLE);
					updateDownloadButton();
					downloading = false;
				} else if (type == 4) {
					// Cancelled
					_progressLayout.setVisibility(View.GONE);
					_download.setVisibility(View.VISIBLE);
					updateDownloadButton();
					downloading = false;
				}

				if (type == 3 || type == 4) {
					PluginDownloadInfo info = PluginListManager
							.getPluginInfo(pluginID);
					if (info != null && plugin.lastUpdate > info.updated) {
						_update.setVisibility(View.VISIBLE);
					} else {
						_update.setVisibility(View.GONE);
					}
				}
			}
		}
	};

	public void updateDownloadButton() {
		PluginDownloadInfo info = PluginListManager.getPluginInfo(pluginID);
		if (info != null) {
			isInstalled = true;
			_download.setText("UNINSTALL");
		} else {
			isInstalled = false;
			_download.setText("DOWNLOAD");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	boolean descExpanded = false;
	boolean descExpandCancel = false;

	public void expandDesc(View view) {
		if (!descExpandCancel) {
			_description.setVisibility(descExpanded ? View.VISIBLE : View.GONE);
			_descriptionHTML.setVisibility(descExpanded ? View.GONE
					: View.VISIBLE);
			_descExpand
					.setImageResource(descExpanded ? R.drawable.ic_action_expand
							: R.drawable.ic_action_collapse);
			descExpanded = !descExpanded;
		} else {
			descExpandCancel = false;
		}
	}

	private void refresh() {
		_loader.setVisibility(View.VISIBLE);
		_details.setVisibility(View.GONE);

		new Thread(new Runnable() {
			@Override
			public void run() {

				StringBuilder sb = new StringBuilder();
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetworkInfo = connectivityManager
						.getActiveNetworkInfo();
				Boolean hasNetwork = activeNetworkInfo != null
						&& activeNetworkInfo.isConnected();

				if (!hasNetwork) {
					Log.d("Plugin details", "No internet connection");
					showError(R.string.plugins_error_nointernet);
					return;
				}

				try {
					URL url = new URL(
							"http://forums.pocketmine.net/api.php?action=getResource&value="
									+ pluginID);
					BufferedReader in = new BufferedReader(
							new InputStreamReader(url.openStream()));
					String str;

					while ((str = in.readLine()) != null) {
						sb.append(str);
					}
					in.close();

					final JSONObject jp = (JSONObject) JSONValue.parse(sb
							.toString());

					versionId = (Long) jp.get("current_version_id");

					dlURL = (String) jp.get("download_url");
					if (dlURL == null || dlURL.equals("")) {
						customDl = false;
						URLConnection dlInfo = new URL(
								"http://forums.pocketmine.net/plugins/"
										+ pluginID + "/download?version="
										+ versionId).openConnection();
						fileSize = dlInfo.getContentLength();
						fileSizeText = getFileSizeAsString(fileSize);
						String ctxDisp = dlInfo
								.getHeaderField("Content-Disposition");
						ctxDisp = ctxDisp.substring(ctxDisp
								.indexOf("filename=\"") + 10);
						ctxDisp = ctxDisp.substring(0, ctxDisp.indexOf("\""));
						fileExt = ctxDisp.substring(ctxDisp.lastIndexOf("."));
						Log.d("Plugin details", "Filename: " + ctxDisp
								+ " Extresion: " + fileExt);
					} else {
						customDl = true;
						fileSize = 0;
						fileSizeText = "Unknown";
						fileExt = "Unknown (External site)";
					}

					reviews = null;
					try {
						reviews = getReviews(pluginID, 1);
					} catch (HttpStatusException statusErr) {
						//
					}

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							_name.setText((String) jp.get("title"));
							_author.setText((String) jp.get("username"));
							_downloads.setText("Downloads: "
									+ (Long) jp.get("download_count"));
							_downloadSize.setText(fileSizeText);
							_rate.setRating(Double.valueOf(
									getDouble(jp.get("rating_avg")))
									.floatValue());
							_rate2.setRating(_rate.getRating());
							_rate2value.setText("Current rating: "
									+ ((double) Math.round(getDouble(jp
											.get("rating_avg")) * 10) / 10));
							_rateCount.setText(""
									+ (Long) jp.get("rating_count"));
							_rate2Count.setText(_rateCount.getText());
							// p.ratedWeighted =
							// getDouble(jp.get("rating_weighted"));
							Date d = new Date(((Long) jp.get("last_update"))
									.longValue() * 1000);
							_lastUpdate.setText(new SimpleDateFormat(
									"d MMM yyyy").format(d));
							// p.tag = ((Long) jp.get("prefix_id")).intValue();
							// p.category = ((Long)
							// jp.get("category_id")).intValue();

							tagLine = (String) jp.get("tag_line");
							description = (String) jp.get("description");
							try {
								description = new SimpleParser()
										.parse(description);
							} catch (Exception e) {
								e.printStackTrace();
							}

							if (!downloading) {
								updateDownloadButton();
								_download.setVisibility(View.VISIBLE);
								PluginDownloadInfo info = PluginListManager
										.getPluginInfo(pluginID);
								if (info != null
										&& plugin.lastUpdate > info.updated) {
									_update.setVisibility(View.VISIBLE);
								} else {
									_update.setVisibility(View.GONE);
								}
							}

							_description.setText(tagLine);

							String version = (String) jp.get("version_string");
							_descriptionHTML.loadData(
									"<html><head></head><body style=\"background:#eee;word-wrap:break-word;\">"
											+ description
											+ "<br/><br/><i>Version: "
											+ version + "<br/>Last updated: "
											+ _lastUpdate.getText()
											+ "<br/>Size: " + fileSizeText
											+ "<br/>File extension: " + fileExt
											+ "</i>" + ClickFixScript
											+ "</body></html>",
									"text/html; charset=UTF-8", null);

							extURL = (String) jp.get("external_url");
							supportURL = (String) jp.get("alt_support_url");

							Log.d("PluginDetails", "ExtURL:" + extURL);

							makeLink(_links, "Plugin's page",
									"http://forums.pocketmine.net/plugins/"
											+ pluginID + "/");

							if (!extURL.equals("")) {
								makeLink(_links, "External page", extURL);
							}
							if (!supportURL.equals("")) {
								makeLink(_links, "Support", supportURL);
							}

							_reviews.removeAllViews();
							LayoutInflater inflater = getLayoutInflater();

							if (reviews != null) {
								for (int i = 0; i < 3; i++) {
									if (reviews.size() > i) {
										reviews.get(i).makeView(inflater,
												_reviews);
									}
								}
							} else {
								Button allReviews = (Button) findViewById(R.id.more_reviews);
								allReviews.setVisibility(View.GONE);
							}

							_loader.setVisibility(View.GONE);
							_details.setVisibility(View.VISIBLE);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					showError(R.string.plugins_error_general);
				}
			}
		}).start();
	}

	public static ArrayList<Review> getReviews(int pluginID, int page)
			throws IOException {
		Document doc = Jsoup
				.connect(
						"http://forums.pocketmine.net/plugins/" + pluginID
								+ "/reviews?page=" + page).timeout(10000).get();
		Elements reviewsElements = doc.select(".review");
		ArrayList<Review> reviews = new ArrayList<Review>();
		for (Element review : reviewsElements) {
			try {
				Review r = new Review();
				r.poster = review.select(".poster").get(0).text();
				r.posterAvatar = review.select(".avatar").select("img")
						.attr("src");
				if (!r.posterAvatar.startsWith("http")) {
					r.posterAvatar = "http://forums.pocketmine.net/"
							+ r.posterAvatar;
				}
				Element messageContent = review.select(".messageContent")
						.get(0);
				r.review = messageContent.select("article").get(0).text();
				Elements response = review.select(".messageResponse");
				if (response.size() > 0) {
					r.response = response.get(0).select("article").get(0)
							.text();
				}
				r.rating = Float.parseFloat(messageContent.select(".ratings")
						.get(0).attr("title"));
				Elements muted = messageContent.select(".muted");
				for (int i = 0; i < muted.size(); i++) {
					String val = muted.get(i).text();
					if (val.startsWith("Version: ")) {
						r.version = val.substring(9);
						break;
					}
				}
				reviews.add(r);
			} catch (Exception e) {
				Log.e("Review parser",
						"Error parsing review; can be a serious error or just found out a bad review.");
				e.printStackTrace();
			}
		}

		try {
			Elements nav = doc.select(".PageNav");
			if (nav.size() > 0) {
				Element el = nav.first();
				int currentPage = Integer.parseInt(el.attr("data-page"));
				int lastPage = Integer.parseInt(el.attr("data-last"));
				if (currentPage < lastPage) {
					Log.d("PageNav", "There are more pages.");

					reviews.add(null);
				}
			}
		} catch (Error e) {
			e.printStackTrace();
		}

		return reviews;
	}

	public View makeLink(ViewGroup parent, String name, String url) {
		View view = getLayoutInflater().inflate(R.layout.plugin_link, parent,
				false);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				view.getLayoutParams());
		params.weight = 1;
		view.setLayoutParams(params);
		TextView _name = (TextView) view.findViewById(R.id.action_text);
		_name.setText(name);
		TextView _value = (TextView) view.findViewById(R.id.action_link);
		_value.setText(url);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				String url = ((TextView) view.findViewById(R.id.action_link))
						.getText().toString();
				view.getContext().startActivity(
						new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
			}
		});
		parent.addView(view);
		return view;
	}

	public Double getDouble(Object o) {
		if (o instanceof Double) {
			return (Double) o;
		} else if (o instanceof Long) {
			return ((Long) o).doubleValue();
		} else if (o instanceof Integer) {
			return ((Integer) o).doubleValue();
		} else {
			return null;
		}
	}

	private void showError(final int resId) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						DetailsActivity.this);
				builder.setTitle(R.string.plugins_error_general);
				builder.setMessage(resId);
				builder.setPositiveButton(R.string.plugins_error_retry,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								refresh();
							}
						});
				builder.setNegativeButton(R.string.plugins_error_cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								finish();
							}
						});
				builder.show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static String getFileSizeAsString(long size) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size
				/ Math.pow(1024, digitGroups))
				+ " " + units[digitGroups];
	}

	public static class Review implements Serializable {
		private static final long serialVersionUID = 1L;

		public String poster;
		public String posterAvatar;
		public String review;
		public String response;
		public float rating;
		public String version;

		public static ArrayList<DownloadAvatarTask> avatars = new ArrayList<DownloadAvatarTask>();

		public View makeView(LayoutInflater inflater, ViewGroup parent) {
			final View view = inflater.inflate(R.layout.review, parent, false);

			TextView authorText = (TextView) view
					.findViewById(R.id.review_author);
			authorText.setText(poster);
			TextView reviewText = (TextView) view
					.findViewById(R.id.review_text);
			reviewText.setText(review);
			RatingBar ratingBar = (RatingBar) view
					.findViewById(R.id.review_rate);
			ratingBar.setRating(rating);
			TextView versionText = (TextView) view
					.findViewById(R.id.review_version);
			versionText.setText("Version: " + version);
			parent.addView(view);
			LinearLayout responseLayout = (LinearLayout) view
					.findViewById(R.id.review_author_response);
			if (response != null) {
				responseLayout.setVisibility(View.VISIBLE);
				TextView responseText = (TextView) view
						.findViewById(R.id.review_author_response_text);
				responseText.setText(response);
			}

			DownloadAvatarTask.downloadAvatar(avatars, posterAvatar,
					(ImageView) view.findViewById(R.id.review_image));

			return view;
		}

	}

	public static class DownloadAvatarTask {
		public String src;
		public ArrayList<ImageView> imageViews = new ArrayList<ImageView>();
		public Bitmap bmp = null;

		public DownloadAvatarTask(String src) {
			this.src = src;
		}

		public static void downloadAvatar(
				ArrayList<DownloadAvatarTask> avatars, String src, ImageView iv) {
			for (DownloadAvatarTask avatar : avatars) {
				if (avatar.src.equals(src)) {
					if (avatar.bmp != null) {
						iv.setImageBitmap(avatar.bmp);
						return;
					}

					avatar.imageViews.add(iv);
					return;
				}
			}

			DownloadAvatarTask task = new DownloadAvatarTask(src);
			task.imageViews.add(iv);
			task.start();
			avatars.add(task);
		}

		public void start() {
			Task task = new Task();
			task.execute(this);
		}

		private class Task extends AsyncTask<DownloadAvatarTask, Void, Bitmap> {
			DownloadAvatarTask task;

			protected Bitmap doInBackground(DownloadAvatarTask... dl) {
				task = dl[0];

				Bitmap bmp = null;
				try {
					InputStream in = new java.net.URL(task.src).openStream();
					bmp = BitmapFactory.decodeStream(in);
				} catch (Exception e) {
					e.printStackTrace();
				}

				task.bmp = bmp;
				return bmp;
			}

			protected void onPostExecute(Bitmap result) {
				for (ImageView view : task.imageViews) {
					view.setImageBitmap(result);
				}
			}
		}
	}
}
