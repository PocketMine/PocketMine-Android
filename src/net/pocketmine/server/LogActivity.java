package net.pocketmine.server;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import net.pocketmine.server.R;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
@android.annotation.TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class LogActivity extends SherlockActivity {

	public static LogActivity logActivity;
	public static ScrollView sv;
	public static SpannableStringBuilder currentLog = new SpannableStringBuilder();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);
		logActivity = this;
		TextView logTV = (TextView) findViewById(R.id.logTextView);
		logTV.setText(currentLog);

		sv = (ScrollView) findViewById(R.id.logScrollView);

		Button btnCmd = (Button) findViewById(R.id.runCommand);
		btnCmd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				EditText et = (EditText) findViewById(R.id.textCmd);
				log(">"
						+ et.getText().toString().replace("&", "&amp;")
								.replace("<", "&lt;").replace(">", "&gt;"));
				ServerUtils.executeCMD(et.getText().toString());
				et.setText("");
			}
		});

	}

	final static int CLEAR_CODE = 143;
	final static int COPY_CODE = CLEAR_CODE + 1;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == CLEAR_CODE) {
			currentLog = new SpannableStringBuilder();
			TextView logTV = (TextView) logActivity
					.findViewById(R.id.logTextView);
			logTV.setText(currentLog);

			return true;
		} else if (item.getItemId() == COPY_CODE) {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(currentLog);
			Toast.makeText(this,
					"Console text was copied to your's clipboard.",
					Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.add(0, COPY_CODE, 0, "Copy").setIcon(R.drawable.content_copy)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menu.add(0, CLEAR_CODE, 0, "Clear").setIcon(R.drawable.content_discard)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	public static void log(final String whatToLog) {
		StringBuilder formatted = new StringBuilder();

		int currentColor = -1;
		Boolean bold = false;

		System.out.println(whatToLog);
		String[] parts = whatToLog.split("\u001B\\[");
		Boolean firstPart = true;
		for (String part : parts) {
			if (!firstPart) {

				int end = part.indexOf("m");
				if (end != -1) {
					String[] flags = part.substring(0, end).split(";");
					for (String flag : flags) {
						if (flag.startsWith(";"))
							flag = flag.substring(1);
						try {
							int n = Integer.parseInt(flag);
							if (n >= 30 && n < 40) {
								// text colour
								if (currentColor != -1) {
									formatted.append("</font>");
									n = -1;
								}
								// get color
								if (!bold) {
									// standard

									if (n == 30)
										currentColor = Color.argb(255, 0, 0, 0);
									else if (n == 31)
										currentColor = Color.argb(255, 128, 0,
												0);
									else if (n == 32)
										currentColor = Color.argb(255, 0, 128,
												0);
									else if (n == 33)
										currentColor = Color.argb(255, 128,
												128, 0);
									else if (n == 34)
										currentColor = Color.argb(255, 0, 0,
												128);
									else if (n == 35)
										currentColor = Color.argb(255, 128, 0,
												128);
									else if (n == 36)
										currentColor = Color.argb(255, 0, 128,
												128);
									else if (n == 37)
										currentColor = Color.argb(255, 128,
												128, 128);
								} else {
									// lighter

									if (n == 30)
										currentColor = Color.argb(255, 85, 85,
												85);
									else if (n == 31)
										currentColor = Color.argb(255, 255, 0,
												0);
									else if (n == 32)
										currentColor = Color.argb(255, 0, 255,
												0);
									else if (n == 33)
										currentColor = Color.argb(255, 255,
												255, 0);
									else if (n == 34)
										currentColor = Color.argb(255, 0, 0,
												255);
									else if (n == 35)
										currentColor = Color.argb(255, 255, 0,
												255);
									else if (n == 36)
										currentColor = Color.argb(255, 0, 255,
												255);
									else if (n == 37)
										currentColor = Color.argb(255, 255,
												255, 255);
								}

								if (currentColor != -1) {
									formatted.append("<font color=\""
											+ (String.format("#%06X",
													(0xFFFFFF & currentColor)))
											+ "\">");
								}
							} else if (n == 0) {
								if (currentColor != -1) {
									formatted.append("</font>");
									n = -1;
								}
								if (bold) {
									bold = false;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					part = part.substring(end + 1);
				}
				formatted.append(part);
			} else {
				formatted.append(part);
				firstPart = false;
			}
		}
		formatted.append("<br/>");
		final Spanned result = Html.fromHtml(formatted.toString());
		currentLog.append(result);

		if (logActivity != null) {
			logActivity.runOnUiThread(new Runnable() {
				public void run() {
					TextView logTV = (TextView) logActivity
							.findViewById(R.id.logTextView);
					logTV.append(result);
					sv.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});
		}
	}

}
