package net.pocketmine.forum;

import java.util.ArrayList;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import net.pocketmine.server.R;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;

public class SearchResultsActivity extends SherlockActivity {

	public ArrayList<PluginsActivity.Plugin> plugins = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plugins_list);

		if (PluginsActivity.plugins == null) {
			Log.e("SearchResultsActivity",
					"Finishing activity, because PluginsActivity's plugin array is empty.");
			finish();
			return;
		}

		int[] pluginsIDs = getIntent().getIntArrayExtra("plugins");
		if (pluginsIDs == null) {
			Log.e("SearchResultsActivity",
					"Finishing activity, because no plugin array given.");
			finish();
			return;
		}

		String query = getIntent().getStringExtra("query");
		if (query != null) {
			getSupportActionBar().setTitle(query);
		}

		try {
			plugins = new ArrayList<PluginsActivity.Plugin>();
			for (int i = 0; i < pluginsIDs.length; i++) {
				plugins.add(PluginsActivity.plugins.get(pluginsIDs[i]));
			}
		} catch (Exception e) {
			Log.e("SearchResultsActivity",
					"Finishing activity, because an stupid error occured.");
			e.printStackTrace();
			finish();
			return;
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

		GridView grid = (GridView) findViewById(R.id.plugins_list);
		grid.setSelector(new ColorDrawable(Color.TRANSPARENT));
		grid.setAdapter(new GridAdapter(this, plugins));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.search_results, menu);
		PluginsActivity.addSearch(this, getSupportActionBar(), menu);
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

}
