package net.pocketmine.forum;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.astuetz.PagerSlidingTabStrip;

import net.pocketmine.forum.DynamicLayout.ShowViewListener;
import net.pocketmine.forum.GridAdapter.DownloadImageTask;
import net.pocketmine.forum.PluginListManager.PluginDownloadInfo;
import net.pocketmine.server.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class PluginsActivity extends SherlockFragmentActivity {

	public static class Plugin {
		public int id;
		public String name;
		public String author;
		public long downloadCount;
		public double rated;
		public int ratedTimes;
		public double ratedWeighted;
		public int lastUpdate;
		public int tag;
		public int category;
		public boolean featured;
		public Bitmap image;
	}

	public static class PluginMatch {
		public int index;
		public Plugin plugin;
		public int score;
	}

	// accent
	public static int color = Color.argb(255, 0, 120, 170);

	PagerSlidingTabStrip tabs;
	ViewPager pager;
	PluginsTabs adapter;

	public static ArrayList<Plugin> plugins;
	public static ArrayList<Plugin> pluginUpdates;
	public static ArrayList<Plugin> featuredPlugins;
	public static ArrayList<Plugin> essentialPlugins;
	public static ArrayList<Plugin> bestRated;
	public static ArrayList<Plugin> topPlugins;
	public static ArrayList<Plugin> topNewPlugins;
	public static ArrayList<Plugin> recentlyUpdated;

	public static PluginsActivity activity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plugins);

		activity = this;

		ActionBar bar = getSupportActionBar();
		Drawable colorDrawable = new ColorDrawable(color);
		Drawable bottomDrawable = getResources().getDrawable(
				R.drawable.actionbar_bottom);
		LayerDrawable ld = new LayerDrawable(new Drawable[] { colorDrawable,
				bottomDrawable });
		bar.setBackgroundDrawable(ld);

		tabs = (PagerSlidingTabStrip) findViewById(R.id.plugins_tabs);
		pager = (ViewPager) findViewById(R.id.plugins_pager);
		adapter = new PluginsTabs(getSupportFragmentManager());
		pager.setAdapter(adapter);
		tabs.setViewPager(pager);
		tabs.setIndicatorColor(color);
		pager.setCurrentItem(1);
		pager.setOffscreenPageLimit(10);

		tabs.setVisibility(View.GONE);
		pager.setVisibility(View.GONE);

		Button errorRetry = (Button) findViewById(R.id.plugins_error_retry);
		errorRetry.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ProgressBar progress = (ProgressBar) findViewById(R.id.plugins_loading);
				LinearLayout layout = (LinearLayout) findViewById(R.id.plugins_error);
				progress.setVisibility(View.VISIBLE);
				layout.setVisibility(View.GONE);
				refresh();
			}
		});

		refresh();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		adapter = null;
	}

	private void refresh() {
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
					Log.d("Plugins", "No internet connection");
					showError(R.string.plugins_error_nointernet);
					return;
				}

				try {
					URL url = new URL(
							"http://forums.pocketmine.net/api.php?action=getResources");
					BufferedReader in = new BufferedReader(
							new InputStreamReader(url.openStream()));
					String str;

					while ((str = in.readLine()) != null) {
						sb.append(str);
					}
					in.close();

					JSONObject jobj = (JSONObject) JSONValue.parse(sb
							.toString());
					JSONArray arr = (JSONArray) jobj.get("resources");
					Log.d("Plugins", "Count: " + arr.size());

					plugins = new ArrayList<Plugin>();
					pluginUpdates = new ArrayList<Plugin>();
					featuredPlugins = new ArrayList<Plugin>();
					essentialPlugins = new ArrayList<Plugin>();
					bestRated = new ArrayList<Plugin>();
					topPlugins = new ArrayList<Plugin>();
					topNewPlugins = new ArrayList<Plugin>();
					recentlyUpdated = new ArrayList<Plugin>();

					for (int i = 0; i < arr.size(); i++) {
						JSONObject jp = (JSONObject) arr.get(i);
						Plugin p = new Plugin();
						p.id = ((Long) jp.get("id")).intValue();
						p.name = (String) jp.get("title");
						p.author = (String) jp.get("author_username");
						p.downloadCount = (Long) jp.get("times_downloaded");
						p.rated = getDouble(jp.get("rating_avg"));
						p.ratedTimes = ((Long) jp.get("times_rated"))
								.intValue();
						p.ratedWeighted = getDouble(jp.get("rating_weighted"));
						p.lastUpdate = ((Long) jp.get("last_update"))
								.intValue();
						p.tag = ((Long) jp.get("prefix_id")).intValue();
						p.category = ((Long) jp.get("category_id")).intValue();
						p.featured = false;

						plugins.add(p);
						bestRated.add(p);
						topPlugins.add(p);

						Log.d("Plugins", "Plugin " + p.name + " found!");
						if ((System.currentTimeMillis() / 1000) - p.lastUpdate < 604800) {
							recentlyUpdated.add(p);
						}

						long created = (Long) jp.get("creation_date");
						if ((System.currentTimeMillis() / 1000) - created < 604800) {
							topNewPlugins.add(p);
						}

						if (jp.get("feature_date") != null) {
							p.featured = true;
							featuredPlugins.add(p);
						}

						if (p.tag == 3) {
							essentialPlugins.add(p);
						}

						PluginDownloadInfo info = PluginListManager
								.getPluginInfo(p.id);
						if (info != null) {
							if (p.lastUpdate > info.updated) {
								// UPDATE!!! UPDATE!!! UPDATE!!!
								pluginUpdates.add(p);
							}
						}
					}

					Collections.sort(bestRated, new Comparator<Plugin>() {
						@Override
						public int compare(Plugin lhs, Plugin rhs) {
							if (lhs.ratedWeighted > rhs.ratedWeighted) {
								return -1;
							} else if (lhs.ratedWeighted < rhs.ratedWeighted) {
								return 1;
							} else {
								return 0;
							}
						}
					});

					Collections.sort(topPlugins, new Comparator<Plugin>() {
						@Override
						public int compare(Plugin lhs, Plugin rhs) {
							if (lhs.downloadCount > rhs.downloadCount) {
								return -1;
							} else if (lhs.downloadCount < rhs.downloadCount) {
								return 1;
							} else {
								return 0;
							}
						}
					});

					Collections.sort(topNewPlugins, new Comparator<Plugin>() {
						@Override
						public int compare(Plugin lhs, Plugin rhs) {
							if (lhs.downloadCount > rhs.downloadCount) {
								return -1;
							} else if (lhs.downloadCount < rhs.downloadCount) {
								return 1;
							} else {
								return 0;
							}
						}
					});

					Collections.sort(recentlyUpdated, new Comparator<Plugin>() {
						@Override
						public int compare(Plugin lhs, Plugin rhs) {
							if (lhs.lastUpdate > rhs.lastUpdate) {
								return -1;
							} else if (lhs.lastUpdate < rhs.lastUpdate) {
								return 1;
							} else {
								return 0;
							}
						}
					});

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							tabs.setVisibility(View.VISIBLE);
							pager.setVisibility(View.VISIBLE);

							ProgressBar progress = (ProgressBar) findViewById(R.id.plugins_loading);
							progress.setVisibility(View.GONE);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					showError(R.string.plugins_error_general);
				}

			}
		}).start();
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

	public void showError(final int resId) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				ProgressBar progress = (ProgressBar) findViewById(R.id.plugins_loading);
				LinearLayout layout = (LinearLayout) findViewById(R.id.plugins_error);
				TextView desc = (TextView) findViewById(R.id.plugins_error_desc);
				progress.setVisibility(View.GONE);
				tabs.setVisibility(View.GONE);
				pager.setVisibility(View.GONE);
				layout.setVisibility(View.VISIBLE);
				desc.setText(resId);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.plugins, menu);
		addSearch(this, getSupportActionBar(), menu);
		return true;
	}

	public void openSettings(MenuItem mi) {
		startActivity(new Intent(this, PluginsSettingsActivity.class));
	}

	public static void addSearch(final Activity activity, ActionBar actionBar,
			Menu menu) {

		SearchView searchView = new SearchView(actionBar.getThemedContext());
		searchView.setQueryHint("Search for plugins");
		AutoCompleteTextView searchText = (AutoCompleteTextView) searchView
				.findViewById(R.id.abs__search_src_text);
		searchText.setHintTextColor(Color.WHITE);
		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				Log.d("Search", query);

				if (query.length() < 3) {
					Toast.makeText(activity, "Enter at least 3 characters.",
							Toast.LENGTH_SHORT).show();
					return false;
				}

				if (plugins == null) {
					return false;
				}

				String[] keywords = query.split(" ");

				int minScore = 0;
				for (String keyword : keywords) {
					minScore += keyword.length() * 2;
				}
				if (minScore - 5 > 0) {
					minScore -= 5;
				}

				ArrayList<PluginMatch> matches = new ArrayList<PluginsActivity.PluginMatch>();
				for (int index = 0; index < plugins.size(); index++) {
					Plugin p = plugins.get(index);
					int score = 0;
					for (String keyword : keywords) {
						String keyword_replaced = keyword.replace("£", "E")
								.replace("$", "S").replace("¢", "C")
								.replace("¥", "Y").replace("€", "E")
								.replace("-", "").toLowerCase(Locale.US);
						String name = p.name.replace("£", "E")
								.replace("$", "S").replace("¢", "C")
								.replace("¥", "Y").replace("€", "E")
								.replace("-", "").toLowerCase(Locale.US);

						int iof = keyword_replaced.indexOf(Character
								.toString(name.charAt(0)));

						if (iof == -1) {
							break;
						}

						for (int i = 0; i < keyword_replaced.length() - iof; i++) {
							// some plugins overuse them

							char c = keyword_replaced.charAt(iof + i);

							if (i >= name.length())
								break;

							char c2 = name.charAt(i);

							if (c == c2) {
								score += 2;
							} else if (i < keyword_replaced.length() - 1
									&& i < name.length() - 1) {
								c2 = name.charAt(i + 1);
								if (c == c2) {
									score += 1;
									i += 1;
								} else if (i < keyword_replaced.length() - 2
										&& i < name.length() - 2) {
									c2 = name.charAt(i + 2);
									if (c == c2) {
										i += 2;
									}
								}
							} else if (i < keyword_replaced.length() - 2
									&& iof + i < name.length() - 2) {
								c2 = name.charAt(iof + i + 2);
								if (c == c2) {
									i += 2;
								}
							}
						}
					}

					if (score > minScore) {
						PluginMatch match = new PluginMatch();
						match.index = index;
						match.plugin = p;
						match.score = score;
						matches.add(match);
					}
				}

				Collections.sort(matches, new Comparator<PluginMatch>() {
					@Override
					public int compare(PluginMatch lhs, PluginMatch rhs) {
						if (lhs.score > rhs.score) {
							return -1;
						} else if (lhs.score < rhs.score) {
							return 1;
						} else {
							return 0;
						}
					}
				});

				if (matches.size() <= 0) {
					Toast.makeText(activity, "No matches found.",
							Toast.LENGTH_SHORT).show();
					return false;
				}

				int[] array = new int[matches.size()];
				for (int i = 0; i < matches.size(); i++) {
					PluginMatch match = matches.get(i);
					Log.d("Match", "Found: " + match.plugin.name
							+ " with score: " + match.score);
					array[i] = match.index;
				}

				Intent i = new Intent(activity, SearchResultsActivity.class);
				i.putExtra("query", query);
				i.putExtra("plugins", array);
				activity.startActivity(i);

				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});
		menu.add("Search")
				.setIcon(R.drawable.ic_search)
				.setActionView(searchView)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_IF_ROOM
								| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
	}

	public class PluginsTabs extends FragmentPagerAdapter {

		public String[] tabs = { "Categories", "Home", "Updates", "Featured",
				"Essential", "Best Plugins", "Top Plugins", "Top New Plugins",
				"Recently updated" };

		public PluginsTabs(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (position == 0) {
				return new CategoriesFragment();
			} else if (position == 1) {
				return new HomeFragment();
			} else if (position == 2) {
				return createFragment(6);
			} else if (position == 3) {
				return createFragment(0);
			} else if (position == 4) {
				return createFragment(1);
			} else if (position == 5) {
				return createFragment(2);
			} else if (position == 6) {
				return createFragment(3);
			} else if (position == 7) {
				return createFragment(4);
			} else if (position == 8) {
				return createFragment(5);
			}
			return new ListFragment();
		}

		private ListFragment createFragment(int id) {
			ListFragment fragment = new ListFragment();
			Bundle args = new Bundle();
			args.putInt("tab", id);
			fragment.setArguments(args);

			return fragment;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return tabs[position];
		}

		@Override
		public int getCount() {
			return tabs.length;
		}

	}

	public static class TestFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			return new LinearLayout(getActivity());
		}

	}

	public static class HomeFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			View v = inflater.inflate(R.layout.activity_plugins_home,
					container, false);
			fillLayout(inflater,
					(DynamicLayout) v.findViewById(R.id.plugins_featured),
					featuredPlugins);
			fillLayout(inflater,
					(DynamicLayout) v.findViewById(R.id.plugins_essential),
					essentialPlugins);
			fillLayout(inflater,
					(DynamicLayout) v.findViewById(R.id.plugins_popular),
					topPlugins);

			Button moreFeatured = (Button) v.findViewById(R.id.more_featured);
			moreFeatured.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					if (activity != null) {
						activity.pager.setCurrentItem(3);
					}
				}
			});

			Button moreEssential = (Button) v.findViewById(R.id.more_essential);
			moreEssential.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					if (activity != null) {
						activity.pager.setCurrentItem(4);
					}
				}
			});

			Button morePopular = (Button) v.findViewById(R.id.more_popular);
			morePopular.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					if (activity != null) {
						activity.pager.setCurrentItem(6);
					}
				}
			});

			return v;
		}

		private void fillLayout(LayoutInflater inflater,
				final DynamicLayout layout, final ArrayList<Plugin> plugins) {
			ArrayList<View> views = new ArrayList<View>();
			if (plugins != null) {
				for (int i = 0; i < plugins.size(); i++) {
					Plugin plugin = plugins.get(i);
					if (plugin != null) {
						View view = GridAdapter.createCard(inflater, layout,
								R.layout.plugin_card2);

						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.FILL_PARENT,
								LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
						view.setLayoutParams(params);

						GridAdapter
								.fillData(getActivity(), view, plugin, false);

						views.add(view);
					}
				}
			}
			layout.setViews(views);

			layout.setOnShowViewListener(new ShowViewListener() {

				@Override
				public void show(int pos, View view) {
					Log.d("PluginsActivity/DownloadImageTask",
							"Downloading at " + pos);
					ImageView image = (ImageView) view
							.findViewById(R.id.plugin_icon);
					view.setTag(new DownloadImageTask(image).execute(plugins
							.get(pos)));
				}

				@Override
				public void hide(int pos) {
					View view = layout.getChildAt(pos);
					if (view != null && view.getTag() != null
							&& view.getTag() instanceof DownloadImageTask) {
						DownloadImageTask task = (DownloadImageTask) view
								.getTag();
						task.cancel = true; // there's no problem, with
											// finishing it to
											// download; just do not change
											// correct
											// image
					}
				}

				@Override
				public void clearAll() {
					for (int i = 0; i < layout.getChildCount(); i++) {
						hide(i);
					}
				}
			});
		}
	}

	public static class CategoriesFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			ListView lv = new ListView(getActivity());

			final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					getActivity(),
					R.layout.plugins_category,
					new String[] { "Admin Tools", "Anti-Griefing Tools",
							"Chat Related", "Developer Tools", "Economy",
							"Fun", "General", "Informational", "Mechanics",
							"Miscellaneous", "Teleportation",
							"World Editing and Management", "World Generators" });
			lv.setAdapter(adapter);
			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> view, View view2,
						int pos, long id) {
					Intent i = new Intent(getActivity(), CategoryActivity.class);
					i.putExtra("category", pos + 3);
					i.putExtra("title", adapter.getItem(pos));
					getActivity().startActivity(i);
				}
			});

			return lv;
		}

	}

	public static class ListFragment extends Fragment {

		public ListFragment() {
			setRetainInstance(false);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			int id = getArguments().getInt("tab", 0);
			ArrayList<Plugin> plugins = null;
			if (id == 0) {
				plugins = featuredPlugins;
			} else if (id == 1) {
				plugins = essentialPlugins;
			} else if (id == 2) {
				plugins = bestRated;
			} else if (id == 3) {
				plugins = topPlugins;
			} else if (id == 4) {
				plugins = topNewPlugins;
			} else if (id == 5) {
				plugins = recentlyUpdated;
			} else if (id == 6) {
				plugins = pluginUpdates;
			}

			if (plugins == null) {
				return new LinearLayout(getActivity()); // whatever
			}

			View v = inflater.inflate(R.layout.activity_plugins_list,
					container, false);
			GridView grid = (GridView) v.findViewById(R.id.plugins_list);
			grid.setSelector(new ColorDrawable(Color.TRANSPARENT));
			grid.setAdapter(new GridAdapter(getActivity(), plugins));

			return v;
		}
	}

}
