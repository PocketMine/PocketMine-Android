package com.MrARM.DroidPocketMine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.http.util.ByteArrayBuffer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.app.ProgressDialog;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;

public class VersionManagerActivity extends SherlockActivity {
	public ArrayAdapter<CharSequence> adapter;
	private Boolean install = false;
	private String notDownloadedStr = " (Not downloaded)";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(install?R.layout.install_versions_activity:R.layout.version_manager);
		
		start();
	}
	
	public void start(){

		install = getIntent().getBooleanExtra("install", false);
		//requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		
		if(install && new File(ServerUtils.getDataDirectory()+"/PocketMine-MP.php").isFile()){
			final Button skipBtn = (Button)findViewById(R.id.skipInstallVer);
			skipBtn.setVisibility(View.VISIBLE);
			skipBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					finish();
				}
			});
		}
		
		final Spinner spin = (Spinner)findViewById(R.id.version_select);
		adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item);
		adapter.add("No version selected");
		
		File verDir = new File(ServerUtils.getDataDirectory()+"/versions/");
		if(!verDir.isDirectory()) verDir.mkdirs();
		File[] farr = verDir.listFiles();
		final List<String> downloadedVers = new ArrayList<String>();
		for(int i=0;i<farr.length;i++){
			File f = farr[i];
			if(f.getName().endsWith(".zip")){
				downloadedVers.add(f.getName().substring(0, f.getName().length()-4));
			}
			//adapter.add(f.getName());
		}
		
		
		final ProgressDialog progress = ProgressDialog.show(this, "Loading versions from server...",
			    "Please wait...", true);
		final VersionManagerActivity ctx = this;
		new Thread(new Runnable() {
			@Override
			public void run() {
				StringBuilder sb = new StringBuilder();
				try 
				{
				    URL url = new URL("https://api.github.com/repos/PocketMine/PocketMine-MP/tags");
				    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
				    String str;
				    
				    while ((str = in.readLine()) != null) 
				    {
				    	sb.append(str);
				    }
				    in.close();
				} catch (Exception e) {
					showToast("Error occured while loading versions from server; waiting 5 seconds and trying again.");
					progress.dismiss();
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					start();
					return;
				}
				progress.dismiss();
				
				final JSONArray array = (JSONArray)JSONValue.parse(sb.toString());
				HomeActivity.ha.runOnUiThread(new Runnable(){
				    public void run(){
				    	
						for(Object obj : array){
							JSONObject jobj = (JSONObject)obj;
							String verName = (String)jobj.get("name");
							Boolean isDown = false;
							
							for(String str : downloadedVers){
								if(str.equals(verName)){
									isDown = true;
									downloadedVers.remove(str);
								}
							}
							
							adapter.add(verName+(isDown?"":notDownloadedStr));
							
						}
						

						for(String str : downloadedVers){
							adapter.add(str+" (Not oficially supported)");
						}
				    }
				});
				//spin.setAdapter(adapter);
			}
		}).start();
		
		spin.setAdapter(adapter);
		
		final Button dlBtn = (Button)findViewById(R.id.dlBtn);
		dlBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				Long sid = spin.getSelectedItemId();
				if(sid == 0){
					return;
				}
				
				CharSequence ver = adapter.getItem(sid.intValue());
				
				Boolean needsDownload = false;
				
				if(ver.length() > notDownloadedStr.length() && ver.toString().substring(ver.length()-notDownloadedStr.length()).contains(notDownloadedStr)){
					ver = ver.subSequence(0, ver.length()-notDownloadedStr.length());
					needsDownload = true;
				}
				
				final CharSequence fver = ver;
				
				if(needsDownload){
					download("https://github.com/PocketMine/PocketMine-MP/archive/"+fver+".zip", fver.toString());
				}else{
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							install(fver);
						}
					}).start();
				}
			}
		});
		
		final Button dlDevBtn = (Button)findViewById(R.id.dlDevBtn);
		dlDevBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				final ProgressDialog progress = ProgressDialog.show(ctx, "Loading versions from server...",
					    "Please wait...", true);
				new Thread(new Runnable() {
					@Override
					public void run() {
						StringBuilder sb = new StringBuilder();
						try 
						{
						    URL url = new URL("https://api.github.com/repos/PocketMine/PocketMine-MP/commits");
						    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
						    String str;
						    
						    while ((str = in.readLine()) != null) 
						    {
						    	sb.append(str);
						    }
						    in.close();
						} catch (Exception e) {
							showToast("Error occured while finding download link.");
							progress.dismiss();
						}
						progress.dismiss();
						
						final JSONArray array = (JSONArray)JSONValue.parse(sb.toString());
						JSONObject obj = (JSONObject) array.get(0);
						String sha = (String) obj.get("sha");
						//spin.setAdapter(adapter);
						download("https://github.com/PocketMine/PocketMine-MP/archive/"+sha+".zip", sha);
					}
				}).start();
			}
		});
	}
	
	private void download(final String address, final String fver) {
		final VersionManagerActivity ctx = this;
		HomeActivity.ha.runOnUiThread(new Runnable() {
			
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
		
							InputStream input = new BufferedInputStream(url.openStream());
							OutputStream output = new FileOutputStream(ServerUtils.getDataDirectory()+"/versions/"+fver+".zip");
		
							byte data[] = new byte[1024];
							long total = 0;
							int count;
							int lastProgress = 0;
							while ((count = input.read(data)) != -1) {
								total += count;
								int progress = (int) (total * 100 / fileLength);
								if(progress != lastProgress) { dlDialog.setProgress(progress); lastProgress = progress; }
								output.write(data, 0, count);
							}
		
							output.flush();
							output.close();
							input.close();
						} catch (Exception e) {
							showToast("Failed to download this version.");
							dlDialog.dismiss();
							return;
						}
						
						dlDialog.dismiss();
						
						install(fver);
						//dlDialog.setTitle("Installing this version...");
						//dlDialog.show();
					}
		
					
				}).start();
			}
		});
	}
	
	
	private void install(CharSequence ver) {
		final VersionManagerActivity ctx = this;
		final CharSequence fver = ver;
		
		HomeActivity.ha.runOnUiThread(new Runnable() {
			
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
						try  {
			            	
			            	
			            	String zipFileName = ServerUtils.getDataDirectory()+"/versions/"+fver+".zip";
			            	ZipFile zip = new ZipFile(zipFileName);
			            	iDialog.setMax(zip.size());
			            	FileInputStream fin = new FileInputStream(zipFileName);       
			            	ZipInputStream zin = new ZipInputStream(fin);
			            	ZipEntry ze = null;
			            	String loc = ServerUtils.getDataDirectory()+"/";
			            	int per = 0;
			            	while ((ze = zin.getNextEntry()) != null) {
			            		per++;
			            		if(!ze.isDirectory()) {
			            			String zeName = ze.getName();
			            			int firstSlash = zeName.indexOf("/");
			            			if(firstSlash != -1){ zeName = zeName.substring(firstSlash+1); }
			            			
			                        java.io.File f = new java.io.File(loc
											+ zeName);
									f = new java.io.File(f.getParent());
									if(!f.isDirectory()) f.mkdirs();
									
			                        BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(loc + zeName));           
			                        for (int c = zin.read(); c != -1; c = zin.read()) {
			                            fout.write(c);
			                        }
			                        zin.closeEntry();
			                        fout.close();
			            		}
			            		iDialog.setProgress(per);
			                }
			            	zin.close();
			            } catch(Exception e) {
			            	showToast("Failed to install this version.");
			            	iDialog.dismiss();
			            	return;
			            }
			            showToast("Installed this version.");
		            	iDialog.dismiss();
		            	HomeActivity.ha.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								ctx.finish();
							}
						});
					}
				}).start();
			}
		});
		
	}
	
	public void showToast(String msg){
		final String fmsg = msg;
		HomeActivity.ha.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), fmsg, Toast.LENGTH_SHORT).show();
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