package com.github.killme2008.utask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * Process list activity
 * 
 * @author dennis<killme2008@gmail.com>
 * 
 */
public class ProcessList extends ListActivity {
	private ProcessHelper processHelper;
	private ApplicationHelper appHelper;
	private HistoryHelper historyHelper;
	private ProgressDialog progressDialog;

	private TextView processLabel;
	private TextView memlabel;

	public static final String CLASSTAG = ProcessList.class.getSimpleName();
	private SimpleAdapter listAdapter;

	private List<Map<String, Object>> processInfos;

	private static final int LOAD_PROCESS_INFOS = 1;

	private List<String> killedApps = Collections.emptyList();

	private List<String> deadKilledApps = Collections.emptyList();

	public static final String LOG_TAG = "UniqTask";

	private final Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == LOAD_PROCESS_INFOS) {
				if (progressDialog != null)
					progressDialog.dismiss();

				progressDialog = null;
				if ((processInfos == null) || processInfos.isEmpty()) {
					setListAdapter(null);
					listItemSelected = null;
					return;
				} else {
					Log.w(LOG_TAG, "There are " + processInfos.size()
							+ " apps running now.");
					listItemSelected = new boolean[processInfos.size()];
					List<String> deadThisTime = new ArrayList<String>(
							killedApps);

					// Selected apps which were killed in history
					for (int i = 0; i < processInfos.size(); i++) {
						String pkgName = (String) processInfos.get(i).get(
								ProcessHelper.PKG_NAME);
						if (killedApps.contains(pkgName)) {
							listItemSelected[i] = true;
							deadThisTime.remove(pkgName);
						}
					}
					deadKilledApps = deadThisTime;

					listAdapter = new SimpleAdapter(ProcessList.this,
							processInfos, R.layout.process, new String[] {
									ProcessHelper.APP_ICON,
									ProcessHelper.APP_NAME }, new int[] {
									R.id.appIcon, R.id.appName }) {

						@Override
						public View getView(int position, View convertView,
								ViewGroup parent) {
							View v = super.getView(position, convertView,
									parent);
							if (listItemSelected[position]) {
								v.setSelected(true);
								v.setPressed(true);
								v.setBackgroundColor(Color.GRAY);
							} else {
								v.setSelected(false);
								v.setPressed(false);
								v.setBackgroundColor(Color.WHITE);
							}
							return v;
						}

					};

					listAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {

						@Override
						public boolean setViewValue(View view, Object data,
								String text) {
							if (view instanceof ImageView
									&& data instanceof Drawable) {
								ImageView imageView = (ImageView) view;
								Drawable drawable = (Drawable) data;
								imageView.setImageDrawable(drawable);
								return true;
							} else
								return false;
						}
					});
					refreshProcessMemInfo();
					setListAdapter(listAdapter);
				}
			}
		}

	};

	static final int ABOUT = Menu.FIRST;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ABOUT, 0, "About").setIcon(R.drawable.about_36);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ABOUT:
			Intent intent = new Intent(this, About.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void refreshProcessMemInfo() {
		ActivityManager.MemoryInfo minfo = new ActivityManager.MemoryInfo();
		processHelper.getActivityManager().getMemoryInfo(minfo);
		processLabel.setText(processInfos.size() + " processes");
		memlabel.setText("Avaiable memory:"
				+ Formatter.formatFileSize(getBaseContext(), minfo.availMem));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.process_list);
		getListView().setOnCreateContextMenuListener(this);
		this.appHelper = new ApplicationHelper(getPackageManager());
		this.processHelper = new ProcessHelper(
				(ActivityManager) getSystemService(ACTIVITY_SERVICE), appHelper);
		this.historyHelper = new HistoryHelper();
		this.processLabel = (TextView) findViewById(R.id.processLabel);
		this.memlabel = (TextView) findViewById(R.id.memLabel);

		Button killButton = (Button) findViewById(R.id.killBtn);
		killButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onKilled();
			}

		});

		Button refreshButton = (Button) findViewById(R.id.refreshBtn);
		refreshButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadItems(getApplicationContext());
			}
		});

	}

	private void onKilled() {
		if (listItemSelected != null) {
			List<String> newKilledApps = new ArrayList<String>();
			int pos = 0;
			for (Boolean selected : listItemSelected) {
				if (selected) {
					Map<String, Object> processInfo = processInfos.get(pos);
					if (processInfo != null) {
						String packgeName = (String) processInfo
								.get(ProcessHelper.PKG_NAME);
						processHelper.killApp(packgeName);
						newKilledApps.add(packgeName);
					}
				}
				pos++;
			}
			// Added dead apps must be killed
			newKilledApps.addAll(this.deadKilledApps);
			// We must sort it!
			Collections.sort(newKilledApps);
			if (!newKilledApps.isEmpty() && !newKilledApps.equals(killedApps)) {
				this.historyHelper.saveKilledPackageNames(
						getApplicationContext(), newKilledApps);
				killedApps = newKilledApps;
			}
			loadItems(getApplicationContext());
		}
	}

	private boolean[] listItemSelected;

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (listItemSelected[position]) {
			listItemSelected[position] = false;
			v.setBackgroundColor(Color.WHITE);
		} else {
			listItemSelected[position] = true;
			v.setBackgroundColor(Color.GRAY);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		loadKilledApps();
		loadItems(getApplicationContext());
	}

	private void loadKilledApps() {
		this.killedApps = this.historyHelper
				.loadKilledPackageNames(getApplicationContext());
		Log.w(LOG_TAG, "Loaded kill history:" + this.killedApps);
	}

	private void loadItems(final Context context) {
		progressDialog = ProgressDialog.show(this,
				getResources().getString(R.string.progress_title),
				getResources().getString(R.string.progress_message));
		new Thread() {
			@Override
			public void run() {
				processInfos = processHelper.getProcessInfos(context);
				handler.sendEmptyMessage(LOAD_PROCESS_INFOS);
			}
		}.start();
	}
}
