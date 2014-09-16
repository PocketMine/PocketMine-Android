package net.pocketmine.forum;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import net.pocketmine.forum.DetailsActivity.Review;
import net.pocketmine.server.R;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class ReviewActivity extends SherlockActivity {

	Boolean hasMore = false;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_review);

		final int pluginID = getIntent().getIntExtra("pluginID", -1);

		Serializable obj = getIntent().getSerializableExtra("reviews");
		if (pluginID == -1 || obj == null || !(obj instanceof ArrayList<?>)) {
			finish();
			// all
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

		ArrayList<Review> reviews = (ArrayList<Review>) obj;

		final LinearLayout layout = (LinearLayout) findViewById(R.id.plugin_reviews);
		final LayoutInflater inflater = getLayoutInflater();
		for (int i = 0; i < reviews.size(); i++) {
			if (reviews.size() > i) {
				if (reviews.get(i) == null) {
					hasMore = true;
				} else {
					reviews.get(i).makeView(inflater, layout);
				}
			}
		}

		DynamicScrollView scroll = (DynamicScrollView) findViewById(R.id.plugin_reviews_scroll);
		scroll.setOnLoadMore(new DynamicScrollView.LoadMoreListener() {
			int page = 1;
			boolean loading = false;

			@Override
			public void onLoadMore() {
				if (hasMore && !loading) {
					loading = true;
					page++;
					hasMore = false;
					final ProgressBar pb = new ProgressBar(ReviewActivity.this);
					pb.setLayoutParams(new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							LinearLayout.LayoutParams.WRAP_CONTENT));
					layout.addView(pb);
					new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								final ArrayList<Review> newReviews = DetailsActivity
										.getReviews(pluginID, page);

								runOnUiThread(new Runnable() {

									@Override
									public void run() {

										layout.removeView(pb);

										for (int i = 0; i < newReviews.size(); i++) {
											if (newReviews.size() > i) {
												if (newReviews.get(i) == null) {
													hasMore = true;
												} else {
													newReviews.get(i).makeView(
															inflater, layout);
												}
											}
										}

										loading = false;
									}
								});
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}).start();
				}
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}
