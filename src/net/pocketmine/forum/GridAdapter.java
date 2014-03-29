package net.pocketmine.forum;

import java.io.InputStream;
import java.util.ArrayList;

import net.pocketmine.server.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GridAdapter extends BaseAdapter {
	private Context ctx;
	private LayoutInflater inflater;
	private ArrayList<PluginsActivity.Plugin> src;

	public GridAdapter(Context c, ArrayList<PluginsActivity.Plugin> plugins) {
		ctx = c;
		src = plugins;
		inflater = (LayoutInflater) c
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return src.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if (convertView == null) {
			view = createCard(inflater, parent, R.layout.plugin_card);
		} else {
			view = convertView;
			if (view.getTag() != null
					&& view.getTag() instanceof DownloadImageTask) {
				DownloadImageTask task = (DownloadImageTask) view.getTag();
				task.cancel = true; // there's no problem, with finishing it to
									// download; just do not change correct
									// image
			}
		}

		PluginsActivity.Plugin plugin = src.get(position);

		fillData((Activity) ctx, view, plugin, true);

		return view;
	}

	public static View createCard(LayoutInflater inflater, ViewGroup parent,
			int resId) {
		View view = inflater.inflate(resId, parent, false);

		return view;
	}

	static boolean touched = false;

	public static void fillData(final Activity a, View view,
			final PluginsActivity.Plugin plugin, Boolean dlImage) {
		final RelativeLayout card = (RelativeLayout) view
				.findViewById(R.id.plugin_card);

		view.setOnTouchListener(new OnTouchListener() {
			boolean cancel = false;

			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if (e.getAction() == MotionEvent.ACTION_DOWN && !touched) {
					cancel = false;
					card.postDelayed(new Runnable() {

						@Override
						public void run() {
							if (!cancel) {
								card.setBackgroundResource(R.drawable.bg_selected_drawable);
							} else {
								cancel = false;
							}
						}
					}, 100);
					touched = true;
					return true;
				} else if (touched
						&& (e.getAction() == MotionEvent.ACTION_UP || e
								.getAction() == MotionEvent.ACTION_CANCEL)) {
					if (e.getAction() == MotionEvent.ACTION_UP) {
						Log.d("GridAdapter", "View clicked.");
						card.setBackgroundResource(R.drawable.bg_selected_drawable);
						Intent i = new Intent(a, DetailsActivity.class);
						i.putExtra("id", plugin.id);
						a.startActivity(i);
					}
					card.postDelayed(new Runnable() {

						@Override
						public void run() {
							touched = false;
							card.setBackgroundResource(R.drawable.bg_drawable);
						}
					}, 50);

					cancel = true;
				}

				return false;
			}
		});

		TextView name = (TextView) view.findViewById(R.id.plugin_name);
		name.setText(plugin.name);

		TextView author = (TextView) view.findViewById(R.id.plugin_author);
		author.setText(plugin.author);

		RatingBar rate = (RatingBar) view.findViewById(R.id.plugin_rate);
		rate.setRating(Double.valueOf(plugin.rated).floatValue());

		TextView rateCount = (TextView) view
				.findViewById(R.id.plugin_rate_count);
		rateCount.setText("" + plugin.ratedTimes);

		ImageView more = (ImageView) view.findViewById(R.id.plugin_more);
		more.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d("GridAdapter", "More clicked.");
			}
		});

		ImageView image = (ImageView) view.findViewById(R.id.plugin_icon);
		if (plugin.image != null) {
			image.setImageBitmap(plugin.image);
		} else {
			image.setImageResource(R.drawable.ic_launcher);
			if (dlImage)
				view.setTag(new DownloadImageTask(image).execute(plugin));
		}
	}

	public static class DownloadImageTask extends
			AsyncTask<PluginsActivity.Plugin, Void, Bitmap> {
		ImageView imageView;
		public Boolean cancel = false;

		public DownloadImageTask(ImageView imageView) {
			this.imageView = imageView;
		}

		protected Bitmap doInBackground(PluginsActivity.Plugin... plugins) {
			if (plugins[0].image != null)
				return plugins[0].image;

			Bitmap icon = null;
			try {
				InputStream in = new java.net.URL(
						"http://forums.pocketmine.net/data/resource_icons/0/"
								+ plugins[0].id + ".jpg").openStream();
				icon = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				e.printStackTrace();
			}
			plugins[0].image = icon;

			return icon;
		}

		protected void onPostExecute(Bitmap result) {
			if (!cancel) {
				if (result != null) {
					imageView.setImageBitmap(result);
				} else {
					imageView.setImageResource(R.drawable.ic_launcher);
				}
			}
		}
	}
}