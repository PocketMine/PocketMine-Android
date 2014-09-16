package net.pocketmine.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import net.pocketmine.server.R;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ConfigActivity extends SherlockActivity {

	private Boolean install = false;

	public LinkedHashMap<String, String> values = null;
	public String ram = "64";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);

		install = getIntent().getBooleanExtra("install", false);

		final CheckBox showAdvanced = (CheckBox) findViewById(R.id.config_advanced);
		final TextView advancedLabel = (TextView) findViewById(R.id.config_advanced_label);
		final LinearLayout advanced = (LinearLayout) findViewById(R.id.config_advanced_layout);
		advanced.setVisibility(View.GONE);
		advancedLabel.setVisibility(View.GONE);
		showAdvanced.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton sender, boolean checked) {
				advanced.setVisibility(checked ? View.VISIBLE : View.GONE);
				advancedLabel.setVisibility(checked ? View.VISIBLE : View.GONE);
			}
		});

		final Button saveBtn = (Button) findViewById(R.id.config_save);
		saveBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				save();
				finish();
			}
		});

		final Button cancelBtn = (Button) findViewById(R.id.config_cancel);
		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		cancelBtn.setVisibility(install ? View.GONE : View.VISIBLE);

		final TextView spawnprotect = (TextView) findViewById(R.id.config_spawnprotect);
		final ToggleButton spawnprotect_toggle = (ToggleButton) findViewById(R.id.config_spawnprotect_enable);
		spawnprotect_toggle
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton sender,
							boolean checked) {
						if (checked) {
							spawnprotect.setEnabled(true);
							spawnprotect.setText("16");
						} else {
							spawnprotect.setEnabled(false);
							spawnprotect.setText("-1");
						}
					}
				});
		final Button whitelist_edit = (Button) findViewById(R.id.config_whitelist_edit); // no
																							// need
																							// to
																							// disable
		whitelist_edit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(ConfigActivity.this,
						WhitelistActivity.class));
			}
		});

		final ToggleButton gamemode_survival = (ToggleButton) findViewById(R.id.config_survival);
		final ToggleButton gamemode_creative = (ToggleButton) findViewById(R.id.config_creative);
		final ToggleButton gamemode_adventure = (ToggleButton) findViewById(R.id.config_adventure);
		final ToggleButton gamemode_spectator = (ToggleButton) findViewById(R.id.config_spectator);

		final ToggleButton ram64 = (ToggleButton) findViewById(R.id.config_ram64);
		final ToggleButton ram128 = (ToggleButton) findViewById(R.id.config_ram128);
		final ToggleButton ram256 = (ToggleButton) findViewById(R.id.config_ram256);
		final ToggleButton ramCustom = (ToggleButton) findViewById(R.id.config_ramCustom);

		final ToggleButton difficulty_peaceful = (ToggleButton) findViewById(R.id.config_peaceful);
		final ToggleButton difficulty_easy = (ToggleButton) findViewById(R.id.config_easy);
		final ToggleButton difficulty_normal = (ToggleButton) findViewById(R.id.config_normal);
		final ToggleButton difficulty_hard = (ToggleButton) findViewById(R.id.config_hard);

		final SeekBar viewDistance = (SeekBar) findViewById(R.id.config_distance);
		final TextView viewDistanceValue = (TextView) findViewById(R.id.config_distance_value);
		final TextView viewDistanceWarning = (TextView) findViewById(R.id.config_distance_warning);
		viewDistance.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			Boolean displayingWarning = true;

			@Override
			public void onStopTrackingTouch(SeekBar bar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar bar) {
			}

			@Override
			public void onProgressChanged(SeekBar bar, int progress,
					boolean fromUser) {
				int val = progress + 3;
				viewDistanceValue.setText("" + val);
				if (val > 10 && !displayingWarning) {
					viewDistanceWarning.setVisibility(View.VISIBLE);
					displayingWarning = true;
				} else if (val <= 10 && displayingWarning) {
					viewDistanceWarning.setVisibility(View.GONE);
					displayingWarning = false;
				}
			}
		});

		readFile();
		setValue(R.id.config_name, "server-name", "Minecraft: PE Server");
		setValue(R.id.config_port, "server-port", "19132");

		gamemode_survival.setChecked(false);
		gamemode_creative.setChecked(false);
		gamemode_adventure.setChecked(false);
		gamemode_spectator.setChecked(false);
		if (values.containsKey("gamemode")) {
			String gamemode = values.get("gamemode");
			if (gamemode.equals("0")) {
				gamemode_survival.setChecked(true);
			} else if (gamemode.equals("1")) {
				gamemode_creative.setChecked(true);
			} else if (gamemode.equals("2")) {
				gamemode_adventure.setChecked(true);
			} else if (gamemode.equals("3")) {
				gamemode_spectator.setChecked(true);
			} else {
				gamemode_survival.setChecked(true);
			}
		} else {
			gamemode_survival.setChecked(true);
		}

		setValue(R.id.config_players, "max-players", "20");
		if (values.containsKey("spawn-protection")
				&& !values.get("spawn-protection").equals("-1")) {

			try {
				spawnprotect.setText(Integer.parseInt(values
						.get("spawn-protection")) + ""); // no, that isn't a
															// nonsense
				spawnprotect_toggle.setChecked(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			spawnprotect_toggle.setChecked(false);
		}
		setValue(R.id.config_whitelist, "whitelist", false);
		setValue(R.id.config_query, "enable-query", true);
		setValue(R.id.config_rcon, "enable-rcon", false);
		setValue(R.id.config_desc, "description",
				"Server made using PocketMine-MP");
		setValue(R.id.config_motd, "motd", "Welcome @player to this server!");
		//setValue(R.id.config_ip, "server-ip", "");
		// *server-type
		ram64.setChecked(false);
		ram128.setChecked(false);
		ram256.setChecked(false);
		ramCustom.setChecked(false);
		if (values.containsKey("memory-limit")) {
			ram = values.get("memory-limit");
			if (ram.endsWith("M")) {
				ram = ram.substring(0, ram.length() - 1);
			}
			if (ram.equals("64")) {
				ram64.setChecked(true);
			} else if (ram.equals("128")) {
				ram128.setChecked(true);
			} else if (ram.equals("256")) {
				ram256.setChecked(true);
			} else {
				try {
					Integer.parseInt(ram);
					ramCustom.setChecked(true);
				} catch (Exception e) {
					ram = "128";
					ram128.setChecked(true);
				}
			}
		} else {
			ram128.setChecked(true);
			ram = "128";
		}

		// *last-update
		setValue(R.id.config_achievements, "announce-player-achievements", true);

		if (values.containsKey("view-distance")) {
			try {
				int v = Integer.parseInt(values.get("view-distance")) - 3;
				if (v >= 0 && v <= 13) {
					viewDistance.setProgress(v);
				}
			} catch (Exception e) {
				e.printStackTrace();
				viewDistance.setProgress(7); // 10-3 = 7
			}
		} else {
			viewDistance.setProgress(7); // 10-3 = 7
		}
		setValue(R.id.config_fly, "allow-flight", false);
		// *spawn-monsters
		// *spawn-mobs
		setValue(R.id.config_hardcore, "hardcore", false);
		setValue(R.id.config_pvp, "pvp", true);

		difficulty_peaceful.setChecked(false);
		difficulty_easy.setChecked(false);
		difficulty_normal.setChecked(false);
		difficulty_hard.setChecked(false);
		if (values.containsKey("difficulty")) {
			String difficulty = values.get("difficulty");
			if (difficulty.equals("0")) {
				difficulty_peaceful.setChecked(true);
			} else if (difficulty.equals("1")) {
				difficulty_easy.setChecked(true);
			} else if (difficulty.equals("2")) {
				difficulty_normal.setChecked(true);
			} else if (difficulty.equals("3")) {
				difficulty_hard.setChecked(true);
			} else {
				difficulty_easy.setChecked(true);
			}
		} else {
			difficulty_easy.setChecked(true);
		}

		setValue(R.id.config_generator_settings, "generator-settings", "");
		setValue(R.id.config_level_name, "level-name", "world");
		setValue(R.id.config_level_seed, "level-seed", "");
		setValue(R.id.config_level_type, "level-type", "DEFAULT");
		setValue(R.id.config_rcon_password, "rcon.password",
				generatePassword(5, 50));
		setValue(R.id.config_autosave, "auto-save", true);
	}

	private void setValue(int resId, String name, String defaultValue) {
		TextView tv = (TextView) findViewById(resId);
		if (values.containsKey(name)) {
			tv.setText(values.get(name));
		} else {
			tv.setText(defaultValue);
		}
	}

	private void setValue(int resId, String name, Boolean defaultValue) {
		CompoundButton tv = (CompoundButton) findViewById(resId);
		if (values.containsKey(name)) {
			tv.setChecked(values.get(name).equals("on"));
		} else {
			tv.setChecked(defaultValue);
		}
	}

	public void save() {
		final ToggleButton gamemode_survival = (ToggleButton) findViewById(R.id.config_survival);
		final ToggleButton gamemode_creative = (ToggleButton) findViewById(R.id.config_creative);
		final ToggleButton gamemode_adventure = (ToggleButton) findViewById(R.id.config_adventure);
		final ToggleButton gamemode_spectator = (ToggleButton) findViewById(R.id.config_spectator);

		final ToggleButton difficulty_peaceful = (ToggleButton) findViewById(R.id.config_peaceful);
		final ToggleButton difficulty_easy = (ToggleButton) findViewById(R.id.config_easy);
		final ToggleButton difficulty_normal = (ToggleButton) findViewById(R.id.config_normal);
		final ToggleButton difficulty_hard = (ToggleButton) findViewById(R.id.config_hard);

		final SeekBar viewDistance = (SeekBar) findViewById(R.id.config_distance);

		putValueString(R.id.config_name, "server-name");
		putValueString(R.id.config_port, "server-port");

		// find out gamemode
		String gamemode = "0";
		if (gamemode_survival.isChecked()) {
			gamemode = "0";
		} else if (gamemode_creative.isChecked()) {
			gamemode = "1";
		} else if (gamemode_adventure.isChecked()) {
			gamemode = "2";
		} else if (gamemode_spectator.isChecked()) {
			gamemode = "3";
		}
		values.put("gamemode", gamemode);

		putValueString(R.id.config_players, "max-players");
		putValueString(R.id.config_spawnprotect, "spawn-protection");
		putValueBool(R.id.config_whitelist, "whitelist");
		putValueBool(R.id.config_query, "enable-query");
		putValueBool(R.id.config_rcon, "enable-rcon");
		//putValueBool(R.id.config_usage, "send-usage");
		putValueString(R.id.config_desc, "description");
		putValueString(R.id.config_motd, "motd");
		//putValueString(R.id.config_ip, "server-ip");
		if (!values.containsKey("server-type"))
			values.put("server-type", "normal");
		values.put("memory-limit", ram + "M");
		if (!values.containsKey("last-update"))
			values.put("last-update", "off");
		putValueBool(R.id.config_achievements, "announce-player-achievements");
		values.put("view-distance", (viewDistance.getProgress() + 3) + "");
		putValueBool(R.id.config_fly, "allow-flight");
		if (!values.containsKey("spawn-animals"))
			values.put("spawn-animals", "on");
		if (!values.containsKey("spawn-mobs"))
			values.put("spawn-mobs", "on");
		putValueBool(R.id.config_hardcore, "hardcore");
		putValueBool(R.id.config_pvp, "pvp");

		String difficulty = "0";
		if (difficulty_peaceful.isChecked()) {
			difficulty = "0";
		} else if (difficulty_easy.isChecked()) {
			difficulty = "1";
		} else if (difficulty_normal.isChecked()) {
			difficulty = "2";
		} else if (difficulty_hard.isChecked()) {
			difficulty = "3";
		}
		values.put("difficulty", difficulty);

		putValueString(R.id.config_generator_settings, "generator-settings");
		putValueString(R.id.config_level_name, "level-name");
		if (values.get("level-name").equals("")) {
			values.put("level-name", "world");
		}
		putValueString(R.id.config_level_seed, "level-seed");
		putValueString(R.id.config_level_type, "level-type");
		putValueString(R.id.config_rcon_password, "rcon.password");
		putValueBool(R.id.config_autosave, "auto-save");

		try {
			PrintWriter writer = new PrintWriter(ServerUtils.getDataDirectory()
					+ "/server.properties");
			for (Map.Entry<String, String> entry : values.entrySet()) {
				writer.println(entry.getKey() + "=" + entry.getValue());
			}
			writer.flush();
			writer.close();
			Toast.makeText(this, "Saved.", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(this, "Saving failed.", Toast.LENGTH_SHORT).show();
		}
	}

	private void putValueString(int resId, String name) {
		TextView tv = (TextView) findViewById(resId);
		values.put(name, tv.getText().toString());
	}

	private void putValueBool(int resId, String name) {
		CompoundButton tv = (CompoundButton) findViewById(resId);
		values.put(name, tv.isChecked() ? "on" : "off");
	}

	public void setGamemode(View v) {
		final ToggleButton gamemode_survival = (ToggleButton) findViewById(R.id.config_survival);
		final ToggleButton gamemode_creative = (ToggleButton) findViewById(R.id.config_creative);
		final ToggleButton gamemode_adventure = (ToggleButton) findViewById(R.id.config_adventure);
		final ToggleButton gamemode_spectator = (ToggleButton) findViewById(R.id.config_spectator);
		gamemode_survival.setChecked(false);
		gamemode_creative.setChecked(false);
		gamemode_adventure.setChecked(false);
		gamemode_spectator.setChecked(false);
		ToggleButton btn = (ToggleButton) v;
		btn.setChecked(true);
	}

	public void setDifficulty(View v) {
		final ToggleButton difficulty_peaceful = (ToggleButton) findViewById(R.id.config_peaceful);
		final ToggleButton difficulty_easy = (ToggleButton) findViewById(R.id.config_easy);
		final ToggleButton difficulty_normal = (ToggleButton) findViewById(R.id.config_normal);
		final ToggleButton difficulty_hard = (ToggleButton) findViewById(R.id.config_hard);
		difficulty_peaceful.setChecked(false);
		difficulty_easy.setChecked(false);
		difficulty_normal.setChecked(false);
		difficulty_hard.setChecked(false);
		ToggleButton btn = (ToggleButton) v;
		btn.setChecked(true);
	}

	public void setRAM(View v) {
		final ToggleButton ram64 = (ToggleButton) findViewById(R.id.config_ram64);
		final ToggleButton ram128 = (ToggleButton) findViewById(R.id.config_ram128);
		final ToggleButton ram256 = (ToggleButton) findViewById(R.id.config_ram256);
		final ToggleButton ramCustom = (ToggleButton) findViewById(R.id.config_ramCustom);

		if (v == ram64 || v == ram128 || v == ram256) {
			ram64.setChecked(false);
			ram128.setChecked(false);
			ram256.setChecked(false);
			ramCustom.setChecked(false);
		}

		if (v == ram64) {
			ram64.setChecked(true);
			ram = "64";
		} else if (v == ram128) {
			ram128.setChecked(true);
			ram = "128";
		} else if (v == ram256) {
			ram256.setChecked(true);
			ram = "256";
		} else if (v == ramCustom) {
			LinearLayout ll = new LinearLayout(this);
			final EditText input = new EditText(this);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			layoutParams.setMargins(HomeActivity.dip2px(8), 0,
					HomeActivity.dip2px(8), 0);
			input.setLayoutParams(layoutParams);
			ll.addView(input);
			new AlertDialog.Builder(this)
					.setTitle("Custom...")
					.setMessage(
							"Select the maximal amount of RAM, which PocketMine can use.")
					.setView(ll)
					.setPositiveButton("Done",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									try {
										String out = input.getText().toString();
										Integer.parseInt(out);
										ram = out;
										ram64.setChecked(false);
										ram128.setChecked(false);
										ram256.setChecked(false);
										ramCustom.setChecked(true);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}).setNegativeButton("Cancel", null).show();
		}
	}

	private void readFile() {
		values = new LinkedHashMap<String, String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					ServerUtils.getDataDirectory() + "/server.properties"));
			try {
				String line;

				while ((line = reader.readLine()) != null) {
					if (!line.startsWith("#")) {
						int iof = line.indexOf("=");
						if (iof == -1) {
							Log.e("Configuration parser", "Invalid entry: "
									+ line);
						} else {
							String name = line.substring(0, iof);
							String value = line.substring(iof + 1);
							Log.d("Configuration parser", "[Parsing] Name: "
									+ name + " Value: " + value);
							values.put(name, value);
						}
					}
				}
			} finally {
				reader.close();
			}
		} catch (FileNotFoundException e) {
			// File not found, it's all
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getSupportMenuInflater().inflate(R.menu.config, menu);
		return true;
	}

	private SecureRandom random = new SecureRandom();
	private char[] chars = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890"
			.toCharArray();

	public String generatePassword(int minLen, int maxLen) {
		int len = random.nextInt(maxLen - minLen) + minLen;
		StringBuilder b = new StringBuilder();

		for (int i = 0; i < len; i++) {
			b.append(chars[random.nextInt(chars.length)]);
		}

		return b.toString();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && install) {

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
