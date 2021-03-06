package com.artifex.mupdf.mini;

import com.artifex.mupdf.fitz.*;
import com.artifex.mupdf.fitz.android.*;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorListener;
import com.skydoves.colorpickerview.sliders.AlphaSlideBar;
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MotionEvent;
import android.view.Menu;
import android.view.MenuInflater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.UnknownHostException;
import java.net.InetAddress;

import java.util.ArrayList;
import java.util.Stack;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


import android.widget.RelativeLayout.LayoutParams;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class DocumentActivity extends Activity
{
	private static final int PERMISSION_REQUEST = 42;
	private final String APP = "MuPDF";

	public final int NAVIGATE_REQUEST = 1;

	protected Worker worker;
	protected SharedPreferences prefs;

	protected Document doc;

	protected String key;
	protected String path;
	protected String mimetype;
	protected byte[] buffer;

	protected boolean hasLoaded;
	protected boolean isReflowable;
	protected boolean fitPage;
	protected String title;
	protected ArrayList<OutlineActivity.Item> flatOutline;
	protected float layoutW, layoutH, layoutEm;
	protected float displayDPI;
	protected int canvasW, canvasH;

	protected View currentBar;
	protected PageView pageView;
	protected View actionBar;
	protected TextView titleLabel;
	protected View searchButton;
	protected View searchBar;
	protected EditText searchText;
	protected View searchCloseButton;
	protected View searchBackwardButton;
	protected View searchForwardButton;
	//protected View zoomButton;
	protected View layoutButton;
	protected PopupMenu layoutPopupMenu;
	protected View outlineButton;
	protected View navigationBar;
	protected TextView pageLabel;
	protected SeekBar pageSeekbar;

	protected int pageCount;
	protected int currentPage;
	protected int searchHitPage;
	protected String searchNeedle;
	protected boolean stopSearch;
	protected Stack<Integer> history;
	protected boolean wentBack;

	private View view;
	public static RelativeLayout item;
	public Context mainContext;
	public static InetAddress ipTargetAddress;
	public static int port = 12777;
	public TextView pointer;
	public ArrayList<String> connectedAddresses = new ArrayList<>();
	protected View hideAllButton;
	protected View undoButton;
	protected View scrollButton;

	protected Switch trailSwitch;
	protected static boolean isTrail = false;

	protected int color = Color.BLUE;
	protected int strokeWidth = 5;
	protected View menuButton;
	protected View toolsButton;
	protected byte[] localHostByteArray = new byte[]{127, 0, 0, 1};
	protected InetAddress localHost;
	protected FileOutputStream fOut = null;
	protected Boolean menuVisible = false;
	protected Boolean toolsVisible = false;
	protected String projectName = "CID-Project";
	protected ColorPickerView colorPickerView;
	protected LinearLayout toolsLayout;
	protected FrameLayout paintViewLayout;
	public ArrayList<PaintView> paintViews = new ArrayList<>();
	private boolean annotationsVisible = true;
	private ArrayList<String> connections;
	private ListAdapter connectionsListAdapter;
	public static String fileLocation;
	public static String projectFileLocation;


	protected String appVersion = "0.1";
	private TextView ipText;
	private EditText portText;

	private LinearLayout menuLayout;

	@SuppressLint("WrongViewCast")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			byte[] ipAddr = new byte[]{127, 0, 0, 1};
			InetAddress addr = InetAddress.getByAddress(ipAddr);
			localHost = InetAddress.getByAddress(localHostByteArray);

			connectedAddresses.add(addr.toString());
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}

		mainContext = getApplicationContext();

		registerReceiver(broadcastReceiver, new IntentFilter("Main.MESSAGE_RECEIVED"));

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		displayDPI = metrics.densityDpi;

		layoutSetup();

		UDP_Server udpServer = new UDP_Server();
		udpServer.port = port;
		udpServer.runUdpServer(mainContext);

        registerReceiver(broadcastReceiver, new IntentFilter("Main.MESSAGE_RECEIVED"));

		actionBar = findViewById(R.id.action_bar);
		searchBar = findViewById(R.id.search_bar);
		navigationBar = findViewById(R.id.navigation_bar);

		currentBar = actionBar;

		Uri uri = getIntent().getData();
		mimetype = getIntent().getType();
		key = uri.toString();

		if (uri.getScheme().equals("file")) {
			title = uri.getLastPathSegment();
			path = uri.getPath();
		} else {
			title = uri.toString();
			try {
				InputStream stm = getContentResolver().openInputStream(uri);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buf = new byte[16384];
				int n;
				while ((n = stm.read(buf)) != -1)
					out.write(buf, 0, n);
				out.flush();
				buffer = out.toByteArray();
			} catch (IOException x) {
				Log.e(APP, x.toString());
				Toast.makeText(this, x.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}

		titleLabel = (TextView)findViewById(R.id.title_label);
		titleLabel.setText(title);

		history = new Stack<Integer>();

		worker = new Worker(this);
		worker.start();

		prefs = getPreferences(Context.MODE_PRIVATE);
		layoutEm = prefs.getFloat("layoutEm", 8);
		fitPage = prefs.getBoolean("fitPage", true);
		currentPage = prefs.getInt(key, 0);
		searchHitPage = -1;
		hasLoaded = false;

		pageView = (PageView)findViewById(R.id.page_view);
		pageView.setActionListener(this);

		pageLabel = (TextView)findViewById(R.id.page_label);
		pageSeekbar = (SeekBar)findViewById(R.id.page_seekbar);
		pageSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public int newProgress = -1;
			public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
				if (fromUser) {
					newProgress = progress;
					pageLabel.setText((progress+1) + " / " + pageCount);
				}
			}
			public void onStartTrackingTouch(SeekBar seekbar) {}
			public void onStopTrackingTouch(SeekBar seekbar) {
				gotoPage(newProgress);
			}
		});

		searchButton = findViewById(R.id.search_button);
		searchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showSearch();
			}
		});
		searchText = (EditText)findViewById(R.id.search_text);
		searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) {
					search(1);
					return true;
				}
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					search(1);
					return true;
				}
				return false;
			}
		});
		searchText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				resetSearch();
			}
		});
		searchCloseButton = findViewById(R.id.search_close_button);
		searchCloseButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				hideSearch();
			}
		});
		searchBackwardButton = findViewById(R.id.search_backward_button);
		searchBackwardButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				search(-1);
			}
		});
		searchForwardButton = findViewById(R.id.search_forward_button);
		searchForwardButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				search(1);
			}
		});

		outlineButton = findViewById(R.id.outline_button);
		outlineButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(DocumentActivity.this, OutlineActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("POSITION", currentPage);
				bundle.putSerializable("OUTLINE", flatOutline);
				intent.putExtras(bundle);
				startActivityForResult(intent, NAVIGATE_REQUEST);
			}
		});

		/*zoomButton = findViewById(R.id.zoom_button);
		zoomButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fitPage = !fitPage;
				loadPage();

			}
		});*/

		hideAllButton = findViewById(R.id.HideAllButton);
		hideAllButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				switchAnnotations();
			}
		});

		undoButton = findViewById(R.id.undoButton);
		undoButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				PaintView pv = paintViews.get(0);
				pv.deleteLastPath(currentPage);
				remoteUndoLastEdit(currentPage);
			}
		});

		menuLayout = new LinearLayout(this);
		LinearLayout.LayoutParams menuLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,30);
		LinearLayout.LayoutParams part = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,70);

		menuLayout.setOrientation(LinearLayout.VERTICAL);
		menuLayoutParams.topMargin = 200;
		addContentView(menuLayout, menuLayoutParams);

		final EditText projectText = new EditText(mainContext);
		LayoutParams projectTextLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		projectText.setLayoutParams(par);
		projectText.setText("CID-Project");
		menuLayout.addView(projectText);

		final Button saveButton = new Button(mainContext);
		saveButton.setLayoutParams(part);
		saveButton.setText("Save");
		menuLayout.addView(saveButton);
		menuLayout.setVisibility(view.INVISIBLE);
		menuLayout.setEnabled(false);

		//Save button exec
		saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				projectName = projectText.getText().toString();

				Log.i("CID", "Save button pressed ");

				if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
						!= PackageManager.PERMISSION_GRANTED) {

					// Should we show an explanation?
					if (shouldShowRequestPermissionRationale(
							Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
					}

					requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
							3);

					// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
					// app-defined int constant
					return;
				}

				writeToFile(Integer.toString(currentPage), projectName);

			}
		});

		ipText = new EditText(mainContext);
		portText = new EditText(mainContext);

		//ip input text field
		LinearLayout.LayoutParams labelTextLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		final TextView ipAddressTitle = new TextView(mainContext);
		ipAddressTitle.setText("IP Address");
		ipAddressTitle.setLayoutParams(part);
		menuLayout.addView(ipAddressTitle);

		LinearLayout.LayoutParams ipTextLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		ipText.setLayoutParams(part);
		ipText.setText("192.168.1.1");
		menuLayout.addView(ipText);

		//port input text field
		final TextView portTitle = new TextView(mainContext);
		portTitle.setText("Port");
		portTitle.setLayoutParams(labelTextLayoutParams);
		menuLayout.addView(portTitle);

		LinearLayout.LayoutParams portTextLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		portText.setLayoutParams(part);
		portText.setText("12777");
		menuLayout.addView(portText);

		final Button connectButton = new Button(mainContext);
		connectButton.setLayoutParams(part);
		connectButton.setText("Add to connections");
		menuLayout.addView(connectButton);
		//menuLayout.setVisibility(view.INVISIBLE);
		//menuLayout.setEnabled(false);

		//connect button exec
		connectButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (checkSelfPermission(Manifest.permission.INTERNET)
						!= PackageManager.PERMISSION_GRANTED) {

					// Should we show an explanation?
					if (shouldShowRequestPermissionRationale(
							Manifest.permission.INTERNET)) {
					}

					requestPermissions(new String[]{Manifest.permission.INTERNET},
							3);

					// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
					// app-defined int constant
					return;
				}
				addAddress("/" + ipText.getText().toString());
				port = Integer.parseInt(portText.getText().toString());
				updateConnectionList();
			}
		});

		//List of connections to be filled
		final TextView connectionsListTitle = new TextView(mainContext);
		connectionsListTitle.setText("Connections");
		connectionsListTitle.setLayoutParams(part);
		menuLayout.addView(connectionsListTitle);

		//need to be visualized and put on top of everything
		connections = new ArrayList<>();
		for (int i = 0; i < connectedAddresses.size(); i++) {
			String connection = connectedAddresses.get(i);
			Log.i("CID", "Connected ip address: " + connection);
			connections.add(connection);
		}
		//updateConnectionList();

		connectionsListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, connections);

		ListView connectionsList = new ListView(mainContext);
		LinearLayout.LayoutParams connectionsListLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		connectionsList.setLayoutParams(part);
		connectionsList.setAdapter(connectionsListAdapter);
		menuLayout.addView(connectionsList);


		connectionsList.setOnItemClickListener(
				new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						//do things with connected ips
						String itemClicked = String.valueOf(parent.getItemAtPosition(position));
						if(itemClicked != "/127.0.0.1") { //avoid sending data to self, this would break local annotation making
							SyncUDP runner = new SyncUDP();
							int sleepTime = 1;
							runner.execute(sleepTime, itemClicked, paintViews);
						}
					}
				}
		);

		menuButton = findViewById(R.id.MenuButton);
		menuButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				menuVisible = !menuVisible;
				if(menuVisible) {
					menuLayout.setVisibility(view.VISIBLE);
					pageView.setVisibility(View.INVISIBLE);
					switchAnnotations();
					updateConnectionList();
				}
				else{
					menuLayout.setVisibility(view.INVISIBLE);
					pageView.setVisibility(View.VISIBLE);
					switchAnnotations();
				}
				menuLayout.setEnabled(menuVisible);
			}
		});

		scrollButton = findViewById(R.id.scrollButton);
		scrollButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				pageView.isScrollActive = !pageView.isScrollActive;
				if(pageView.isScrollActive){
					scrollButton.setBackgroundColor(Color.RED);
				}
				else{
					scrollButton.setBackgroundColor(Color.TRANSPARENT);
				}
			}
		});

		trailSwitch = (Switch) findViewById(R.id.trailSwitch);
		trailSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				isTrail = isChecked;
			}
		});

		paintViewLayout = (FrameLayout) findViewById(R.id.paintViewLayout);
		toolsLayout = (LinearLayout) findViewById(R.id.toolsLayout);

		toolsButton = findViewById(R.id.ToolsButton);
		toolsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				toggleAnnotationInteraction();
			}

		});

		//create colorpicker and controls
		colorPickerView = (ColorPickerView) findViewById(R.id.colorPickerView);
		AlphaSlideBar alphaSlideBar = (AlphaSlideBar) findViewById(R.id.alphaSlideBar);
		colorPickerView.attachAlphaSlider(alphaSlideBar);
		BrightnessSlideBar brightnessSlideBar = (BrightnessSlideBar) findViewById(R.id.brightnessSlide);
		colorPickerView.attachBrightnessSlider(brightnessSlideBar);

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				colorPickerView.selectByHsv(Color.RED);
				toolsLayout.setVisibility(view.INVISIBLE);
			}
		}, 100);

		colorPickerView.setColorListener(new ColorListener() {
			@Override
			public void onColorSelected(int chosenColor, boolean fromUser) {
				color = chosenColor;
			}
		});

		layoutButton = findViewById(R.id.layout_button);
		layoutPopupMenu = new PopupMenu(this, layoutButton);
		layoutPopupMenu.getMenuInflater().inflate(R.menu.layout_menu, layoutPopupMenu.getMenu());
		layoutPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				float oldLayoutEm = layoutEm;
				int id = item.getItemId();
				if (id == R.id.action_layout_6pt) layoutEm = 6;
				else if (id == R.id.action_layout_7pt) layoutEm = 7;
				else if (id == R.id.action_layout_8pt) layoutEm = 8;
				else if (id == R.id.action_layout_9pt) layoutEm = 9;
				else if (id == R.id.action_layout_10pt) layoutEm = 10;
				else if (id == R.id.action_layout_11pt) layoutEm = 11;
				else if (id == R.id.action_layout_12pt) layoutEm = 12;
				else if (id == R.id.action_layout_13pt) layoutEm = 13;
				else if (id == R.id.action_layout_14pt) layoutEm = 14;
				else if (id == R.id.action_layout_15pt) layoutEm = 15;
				else if (id == R.id.action_layout_16pt) layoutEm = 16;
				if (oldLayoutEm != layoutEm)
					relayoutDocument();
				return true;
			}
		});

		layoutButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				layoutPopupMenu.show();
			}
		});

		//connectedAddresses.add(ipTargetAddress.toString());

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case 1 : {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					// permission was granted, yay! Do the
					// contacts-related task you need to do.
					Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
					finish();
					//if (currentPage >= 0) {
						//for (int i = 0; i < paintViews.size(); i++) {
							//paintViews.get(i).saveCurrentPage(currentPage); //this is for png save
							//paintViews.get(i).writeToFile(Integer.toString(currentPage), projectName);
					writeToFile(Integer.toString(currentPage), projectName);

					//writeToFile(projectText.getText().toString(), Integer.toString(currentPage));
						//}
					//}
				} else {

					// permission denied. Disable the
					// functionality that depends on this permission.
					Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
				}
				return;
			}

			// other 'case' lines to check for other
			// permissions this app might request
		}
	}

	private void updateConnectionList() {
		connections.clear();
		for (int i = 0; i < connectedAddresses.size(); i++) {
			String connection = connectedAddresses.get(i).toString();
			Log.i("CID", "Connected ip address: " + connection);
			connections.add(connection);
			((ArrayAdapter<String>) connectionsListAdapter).notifyDataSetChanged();
		}
	}

	public void toggleAnnotationInteraction(){
		toolsVisible = !toolsVisible; 

		if(toolsVisible) {
			toolsLayout.setVisibility(view.VISIBLE);
			toolsButton.setBackgroundColor(Color.RED);
		}
		else{
			toolsLayout.setVisibility(view.INVISIBLE);
			toolsButton.setBackgroundColor(Color.TRANSPARENT);

		}
		toolsLayout.setEnabled(toolsVisible);
	}

	public void layoutSetup(){
		//CID Variables
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.document_activity, null);
		item = (RelativeLayout ) view.findViewById(R.id.mainRelativeLayout);
		setContentView(item);
	}

	public void onPageViewSizeChanged(int w, int h) {
		canvasW = w;
		canvasH = h;
		layoutW = canvasW * 72 / displayDPI;
		layoutH = canvasH * 72 / displayDPI;

		if (!hasLoaded) {
			hasLoaded = true;
			openDocument();

		} else if (isReflowable) {
			relayoutDocument();
			//paintview resize
			fitPaintViews();
		} else {
			loadPage();
			//paintview resize
			fitPaintViews();
		}
	}

	protected void openDocument() {
		worker.add(new Worker.Task() {
			boolean needsPassword;
			public void work() {
				Log.i(APP, "open document");
				if (path != null) {
					doc = Document.openDocument(path);
				}
				else
					doc = Document.openDocument(buffer, mimetype);
				needsPassword = doc.needsPassword();
			}
			public void run() {
				if (needsPassword)
					askPassword(R.string.dlog_password_message);
				else {
					loadDocument();

				}
			}
		});
	}

	protected void askPassword(int message) {
		final EditText passwordView = new EditText(this);
		passwordView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
		passwordView.setTransformationMethod(PasswordTransformationMethod.getInstance());

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dlog_password_title);
		builder.setMessage(message);
		builder.setView(passwordView);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				checkPassword(passwordView.getText().toString());
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				finish();
			}
		});
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		});
		builder.create().show();
	}

	protected void checkPassword(final String password) {
		worker.add(new Worker.Task() {
			boolean passwordOkay;
			public void work() {
				Log.i(APP, "check password");
				passwordOkay = doc.authenticatePassword(password);
			}
			public void run() {
				if (passwordOkay)
					loadDocument();
				else
					askPassword(R.string.dlog_password_retry);
			}
		});
	}

	public void onPause() {
		super.onPause();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putFloat("layoutEm", layoutEm);
		editor.putBoolean("fitPage", fitPage);
		editor.putInt(key, currentPage);
		editor.commit();
	}

	public void onBackPressed() {
		//if (history.empty()) {
			//super.onBackPressed();
		//} else {
			//currentPage = history.pop();
			//loadPage();

			if (menuVisible){
				menuLayout.setVisibility(view.INVISIBLE);
				pageView.setVisibility(View.VISIBLE);
				switchAnnotations();
				updateConnectionList();
				menuVisible = false;
			}
			else if(toolsVisible) {
				//toolsLayout.setVisibility(view.INVISIBLE);
				toggleAnnotationInteraction();
				//switchAnnotations();
			}
			//else if(){
				//if undo is possible, undo
			//}
			else{
				super.onBackPressed();
			}
		//}
	}

	public void onActivityResult(int request, int result, Intent data) {
		if (request == NAVIGATE_REQUEST && result >= RESULT_FIRST_USER)
			gotoPage(result - RESULT_FIRST_USER);
	}

	protected void showKeyboard() {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.showSoftInput(searchText, 0);
	}

	protected void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
	}

	protected void resetSearch() {
		stopSearch = true;
		searchHitPage = -1;
		searchNeedle = null;
		pageView.resetHits();
	}

	protected void runSearch(final int startPage, final int direction, final String needle) {
		stopSearch = false;
		worker.add(new Worker.Task() {
			int searchPage = startPage;
			public void work() {
				if (stopSearch || needle != searchNeedle)
					return;
				for (int i = 0; i < 9; ++i) {
					Log.i(APP, "search page " + searchPage);
					Page page = doc.loadPage(searchPage);
					Quad[] hits = page.search(searchNeedle);
					page.destroy();
					if (hits != null && hits.length > 0) {
						searchHitPage = searchPage;
						break;
					}
					searchPage += direction;
					if (searchPage < 0 || searchPage >= pageCount)
						break;
				}
			}
			public void run() {
				if (stopSearch || needle != searchNeedle) {
					pageLabel.setText((currentPage+1) + " / " + pageCount);
				} else if (searchHitPage == currentPage) {
					loadPage();
				} else if (searchHitPage >= 0) {
					history.push(currentPage);
					currentPage = searchHitPage;
					loadPage();
				} else {
					if (searchPage >= 0 && searchPage < pageCount) {
						pageLabel.setText((searchPage+1) + " / " + pageCount);
						worker.add(this);
					} else {
						pageLabel.setText((currentPage+1) + " / " + pageCount);
						Log.i(APP, "search not found");
						Toast.makeText(DocumentActivity.this, getString(R.string.toast_search_not_found), Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

	}

	protected void search(int direction) {
		hideKeyboard();
		int startPage;
		if (searchHitPage == currentPage)
			startPage = currentPage + direction;
		else
			startPage = currentPage;
		searchHitPage = -1;
		searchNeedle = searchText.getText().toString();
		if (searchNeedle.length() == 0)
			searchNeedle = null;
		if (searchNeedle != null)
			if (startPage >= 0 && startPage < pageCount)
				runSearch(startPage, direction, searchNeedle);
	}

	protected void loadDocument() {
		worker.add(new Worker.Task() {
			public void work() {
				try {
					Log.i(APP, "load document");
					String metaTitle = doc.getMetaData(Document.META_INFO_TITLE);
					if (metaTitle != null)
						title = metaTitle;
					isReflowable = doc.isReflowable();
					if (isReflowable) {
						Log.i(APP, "layout document");
						doc.layout(layoutW, layoutH, layoutEm);
					}
					pageCount = doc.countPages();


				} catch (Throwable x) {
					doc = null;
					pageCount = 1;
					currentPage = 0;
					throw x;
				}
			}
			public void run() {
				if (currentPage < 0 || currentPage >= pageCount) {
					currentPage = 0;
				}
				titleLabel.setText(title);
				if (isReflowable) {
					layoutButton.setVisibility(View.VISIBLE);
				}
				else{
					//zoomButton.setVisibility(View.VISIBLE);
				}
				loadPage();
				loadOutline();
				//Paintview hook up
				changePageDrawing();
			}
		});
	}

	protected void relayoutDocument() {
		worker.add(new Worker.Task() {
			public void work() {
				try {
					long mark = doc.makeBookmark(currentPage);
					Log.i(APP, "relayout document");
					doc.layout(layoutW, layoutH, layoutEm);
					pageCount = doc.countPages();
					currentPage = doc.findBookmark(mark);
				} catch (Throwable x) {
					pageCount = 1;
					currentPage = 0;
					throw x;
				}
			}
			public void run() {
				loadPage();
				loadOutline();
				changePageDrawing();
			}
		});
	}

	private void loadOutline() {
		worker.add(new Worker.Task() {
			private void flattenOutline(Outline[] outline, String indent) {
				for (Outline node : outline) {
					if (node.title != null)
						flatOutline.add(new OutlineActivity.Item(indent + node.title, node.page));
					if (node.down != null)
						flattenOutline(node.down, indent + "    ");
				}
			}
			public void work() {
				Log.i(APP, "load outline");
				Outline[] outline = doc.loadOutline();
				if (outline != null) {
					flatOutline = new ArrayList<OutlineActivity.Item>();
					flattenOutline(outline, "");
				} else {
					flatOutline = null;
				}
			}
			public void run() {
				if (flatOutline != null) {
					outlineButton.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	protected void loadPage() {
		final int pageNumber = currentPage;
		stopSearch = true;
		worker.add(new Worker.Task() {
			public Bitmap bitmap;
			public Link[] links;
			public Quad[] hits;
			public void work() {
				try {
					Log.i(APP, "load page " + pageNumber);
					Page page = doc.loadPage(pageNumber);
					Log.i(APP, "draw page " + pageNumber);
					Matrix ctm;
					if (fitPage) {
						ctm = AndroidDrawDevice.fitPage(page, canvasW, canvasH);
					}
					else {
						ctm = AndroidDrawDevice.fitPageWidth(page, canvasW);
					}
					bitmap = AndroidDrawDevice.drawPage(page, ctm);
					links = page.getLinks();
					if (links != null)
						for (Link link : links)
							link.bounds.transform(ctm);
					if (searchNeedle != null) {
						hits = page.search(searchNeedle);
						if (hits != null)
							for (Quad hit : hits)
								hit.transform(ctm);
					}
				} catch (Throwable x) {
					Log.e(APP, x.getMessage());
				}
			}
			public void run() {
				if (bitmap != null) {
					pageView.setBitmap(bitmap, wentBack, links, hits);
				}
				else {
					pageView.setError();
				}
				pageLabel.setText((currentPage+1) + " / " + pageCount);
				pageSeekbar.setMax(pageCount - 1);
				pageSeekbar.setProgress(pageNumber);
				wentBack = false;
				fitPaintViews();
				changePageDrawing();
			}
		});
	}

	protected void showSearch() {
		currentBar = searchBar;
		actionBar.setVisibility(View.GONE);
		searchBar.setVisibility(View.VISIBLE);
		searchBar.requestFocus();
		showKeyboard();
	}

	protected void hideSearch() {
		currentBar = actionBar;
		actionBar.setVisibility(View.VISIBLE);
		searchBar.setVisibility(View.GONE);
		hideKeyboard();
		resetSearch();
	}

	//menu interaction trigger
	public void toggleUI() {
		if (navigationBar.getVisibility() == View.VISIBLE) {
			currentBar.setVisibility(View.GONE);
			navigationBar.setVisibility(View.GONE);
			toolsLayout.setVisibility(View.GONE);
			toolsVisible = false;
			if (currentBar == searchBar)
				hideKeyboard();
		} else {
			currentBar.setVisibility(View.VISIBLE);
			navigationBar.setVisibility(View.VISIBLE);

			if (currentBar == searchBar) {
				searchBar.requestFocus();
				showKeyboard();
			}
		}
	}

	public void goBackwardLocal(){
		if (currentPage > 0) {
			for (int i = 0; i < paintViews.size(); i++) {
				paintViews.get(i).saveCurrentPage(currentPage);
			}
			wentBack = true;
			currentPage--;
			loadPage();

            changePageDrawing();

        }
	}

	public void goForwardLocal(){
			if (currentPage < pageCount - 1) {
				for (int i = 0; i < paintViews.size(); i++) {
					paintViews.get(i).saveCurrentPage(currentPage);
				}
				currentPage++;
				loadPage();

                changePageDrawing();
			}
	}

	public void gotoPageLocal(int p) {
		if (p >= 0 && p < pageCount && p != currentPage) {
			for (int i = 0; i < paintViews.size(); i++) {
				paintViews.get(i).saveCurrentPage(currentPage);
			}
			history.push(currentPage);
			currentPage = p;
			loadPage();

			changePageDrawing();
		}
	}

	public void changePageDrawing(){
		for (int i = 0; i < paintViews.size(); i++) {
			//Log.i("CID", currentPage + "");
			paintViews.get(i).changePage(currentPage);
		}
    }

	public void goBackward() {
		if (currentPage > 0) {
			goBackwardLocal();

			UDP_Client udpClient = new UDP_Client();
			/*udpClient.addr = ipTargetAddress;
			udpClient.port = port;
			udpClient.Message = "goToPage," + (currentPage);
			udpClient.Send();*/

			sendToAll(/*udpClient,*/ "goToPage," + (currentPage));

		}
	}

	public void goForward() {
		if (currentPage < pageCount - 1) {
			goForwardLocal();

			UDP_Client udpClient = new UDP_Client();
			/*udpClient.addr = ipTargetAddress;
			udpClient.port = port;
			udpClient.Message = "goToPage," + (currentPage);
			udpClient.Send();
			 */

			sendToAll(/*udpClient,*/ "goToPage," + (currentPage));

		}
	}

	public void gotoPage(int p) {
		gotoPageLocal(p);


		UDP_Client udpClient = new UDP_Client();
		/*
		udpClient.addr = ipTargetAddress;
		udpClient.port = port;
		udpClient.Message = "goToPage," + p;
		udpClient.Send();
		 */

		sendToAll(/*udpClient,*/ "goToPage," + p);
	}

	public void gotoURI(String uri) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET); // FLAG_ACTIVITY_NEW_DOCUMENT in API>=21
		try {
			startActivity(intent);
		} catch (Throwable x) {
			Log.e(APP, x.getMessage());
			Toast.makeText(DocumentActivity.this, x.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	public String[] remoteParse(String RemoteMessage){
		String[] splitMessage;
		splitMessage = RemoteMessage.split(",");
		String _temp = "";

		for (int i = 0; i < splitMessage.length; i++){
			_temp += splitMessage[i] + " ";
		}

		//check if remote command comes from new ip
		InetAddress ip = null;
		try {
			ip = InetAddress.getByName(splitMessage[0].substring(1));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		addAddress(ip.toString());

		return splitMessage;
	}

	private void addAddress(String ip){
		if (!connectedAddresses.contains(ip.toString())) {
			connectedAddresses.add(ip.toString());
			updateConnectionList();
			//create a new paintview with new connected IP as ID
			Log.e("CID", "CREATING REMOTE PV FOR " + ip.toString());
			createRemoteGraphics(ip.toString());
		}
	}

	private String currentMessage;

	private String previousMessage = "";

	BroadcastReceiver broadcastReceiver;
	{
		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// internet lost alert dialog method call from here...
				Bundle messagesReceived = intent.getExtras();

				if (messagesReceived == null) {
					currentMessage = null;
				} else {
					currentMessage = messagesReceived.getString("Main.MESSAGE_STRING");
				}

				//do operations with received message
				String[] parsedMessage = remoteParse(currentMessage);


				if(!currentMessage.equals(previousMessage) || parsedMessage[1].equals("undo")){

				InetAddress ip = null;
				try {
					ip = InetAddress.getByName(parsedMessage[0].substring(1));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}

				////////////////////////////////////////////////////////////////////////////////////////////
				//parsedMessage[0] is the remote user's ip
				//parsedMessage[1] is the command
				//the others are the command's parameters
				///////////////////////////////////////////////////////////////////////////////////////////
				//Log.i("CID", "Receiving: " + parsedMessage);
				switch (parsedMessage[1]) {
					case "goForward":
						goForwardLocal();
						break;
					case "goBackward":
						goBackwardLocal();
						break;
					case "goToPage":
						gotoPageLocal(Integer.parseInt(parsedMessage[2]));
						break;
					case "printOnScreen":
						printOnScreenLocal(Integer.parseInt(parsedMessage[2]), Integer.parseInt(parsedMessage[3]));
						break;
					case "drawOnScreen":
						drawOnScreenRemote(ip.toString(), parsedMessage[2], Integer.parseInt(parsedMessage[3]), Float.parseFloat(parsedMessage[4]), Float.parseFloat(parsedMessage[5]), Integer.parseInt(parsedMessage[6]), Integer.parseInt(parsedMessage[7]), Boolean.parseBoolean(parsedMessage[8]));
						break;
					case "undo":
						PaintView pv = findPaintViewByIpAddress(ip.toString());
						pv.deleteLastPath(Integer.parseInt(parsedMessage[2]));

						//pv.deleteLastPathOnPage(Integer.parseInt(parsedMessage[2]));
						break;
					default:
						//default
				}
				}
				else{
					//discard doubles
					//Log.i("tag", "MESSAGE WAS THE SAME " + new String(lMsg, 0, dp.getLength()));
				}
				previousMessage = currentMessage;
			}
		};
	}

	public void printOnScreen(int x, int y){
		if (pointer == null){
			pointer = new TextView(this);
			pointer.setText("0");
			item.addView(pointer);
		}
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		layoutParams.topMargin = y;
		layoutParams.leftMargin = x;
		pointer.setLayoutParams(layoutParams);
	}

	private void printOnScreenLocal(int x, int y){
		printOnScreen(x, y);
	}

	//this is where you detect touch on page
	long startTime;

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		float x = event.getRawX();
		float y = event.getRawY();

		View v = getCurrentFocus();
		if(!pageView.isScrollActive){
			if (y >= (canvasH - pageView.bitmapH) / 2f && y <= ((canvasH - pageView.bitmapH) / 2f) + pageView.bitmapH) {

				if ((y < actionBar.getHeight() || y > canvasH - actionBar.getHeight()) && actionBar.getVisibility() == View.VISIBLE) {
					//clicking on menu bars, ignoring touch for drawing
				} else {
					//clicking on bitmap, drawing
					float percX;
					float percY;

					float horizontalOffset = (canvasW - pageView.bitmapW) / 2f;
					float verticalOffset = (canvasH - pageView.bitmapH) / 2f;

					if ((pageView.canvasW - pageView.bitmapW) >= 0f && pageView.viewScale != 1f) {
						horizontalOffset = (pageView.canvasW - pageView.bitmapW);
						x = x - horizontalOffset * pageView.viewScale;
					} else if ((pageView.canvasW - pageView.bitmapW) >= 0f && pageView.viewScale == 1f) {
						x = x - horizontalOffset;
					}
					if ((pageView.canvasH - pageView.bitmapH) >= 0f && pageView.viewScale != 1f) {
						verticalOffset = (pageView.canvasH - pageView.bitmapH);
						y = y - verticalOffset * pageView.viewScale;
					} else if ((pageView.canvasH - pageView.bitmapH) >= 0f && pageView.viewScale == 1f) {
						y = y - verticalOffset;
					}

					percX = (x + pageView.scrollX) / pageView.bitmapW;
					percY = (y + pageView.scrollY) / pageView.bitmapH;

					//Log.i("CID", toolsVisible + " " + menuVisible);
					if (!menuVisible && !toolsVisible) {
						if (event.getPointerCount() == 2) {
							fitPaintViews();
							startTime = System.currentTimeMillis();
						}
						//get number of fingers on screen and filter, this is a way to filter pinch to zoom mistakingly drawing
						else if (event.getPointerCount() == 1 && System.currentTimeMillis() - startTime > 2) {
							switch (event.getAction()) {
								case MotionEvent.ACTION_DOWN:
									if (annotationsVisible) {

										drawOnScreenLocal("ACTION_DOWN", currentPage, percX, percY, strokeWidth, color, isTrail);
										remoteDrawOnScren("ACTION_DOWN", currentPage, percX, percY, strokeWidth, color, isTrail);

									}

									break;
								case MotionEvent.ACTION_MOVE:
									if (annotationsVisible) {
										drawOnScreenLocal("ACTION_MOVE", currentPage, percX, percY, strokeWidth, color, isTrail);
										remoteDrawOnScren("ACTION_MOVE", currentPage, percX, percY, strokeWidth, color, isTrail);
									}
									break;
								case MotionEvent.ACTION_UP:

									if (annotationsVisible) {
										drawOnScreenLocal("ACTION_UP", currentPage, percX, percY, strokeWidth, color, isTrail);
										remoteDrawOnScren("ACTION_UP", currentPage, percX, percY, strokeWidth, color, isTrail);
									}

									startTime = System.currentTimeMillis();
									break;
							}
						} else if (event.getPointerCount() == 2) {
							fitPaintViews();
							startTime = System.currentTimeMillis();
						}
					}
				}
			}
		}
		boolean ret = super.dispatchTouchEvent(event);
		return ret;
	}

	public void drawOnScreenLocal(String action, int pageNumber, float x, float y, int strokeWidth, int color, boolean isTrail){

		paintViews.get(0).actionPages.get(pageNumber).add(action + "," + pageNumber + "," + x + "," + y + "," + strokeWidth + "," + color + "," + isTrail);

		//record actions in file for local save data
		x = (x * pageView.bitmapW) - pageView.scrollX;
		y = (y * pageView.bitmapH) - pageView.scrollY;

		switch(action) {
			//if ACTION DOWN happens outside of bounds, there will be now touchstart but there will be a touchMove
			case "ACTION_DOWN":
				if (annotationsVisible) {
					paintViews.get(0).strokeWidth = strokeWidth;
					paintViews.get(0).currentColor = color;
					paintViews.get(0).touchStart(pageNumber, x, y);
					paintViews.get(0).invalidate();
				}
				break;
			case "ACTION_MOVE":
				if (annotationsVisible) {
					paintViews.get(0).touchMove(x, y);
					paintViews.get(0).invalidate();
				}
				break;
			case "ACTION_UP":
				if (annotationsVisible) {
					paintViews.get(0).touchUp(isTrail);
					paintViews.get(0).invalidate();
				}
				break;
		}
	}

	public void drawOnScreenRemote(String ip, String action, int pageNumber, float x, float y, int recvStrokeWidth, int recvColor, boolean isLineTrail){

		PaintView pv = findPaintViewByIpAddress(ip);
		//record actions in file for remote save data
		paintViews.get(paintViews.indexOf(pv)).actionPages.get(pageNumber).add(action + "," + pageNumber + "," + x + "," + y + "," + recvStrokeWidth + "," + recvColor + "," + isLineTrail);

		x = (x * pageView.bitmapW) - pageView.scrollX;
		y = (y * pageView.bitmapH) - pageView.scrollY;

		switch(action) {
			case "ACTION_DOWN":
				if (annotationsVisible) {
					pv.currentColor = recvColor;
					pv.strokeWidth = recvStrokeWidth;
					//Log.i("CID", Float.toString(percX - pageView.offsetX)  + " " + Float.toString(percY - pageView.offsetY));
					pv.touchStart(pageNumber, x, y);
					pv.invalidate();
				}
				break;
			case "ACTION_MOVE":
				if (annotationsVisible) {
					pv.touchMove(x, y);
					pv.invalidate();
				}
				break;
			case "ACTION_UP":
				if (annotationsVisible) {
					pv.touchUp(isLineTrail);
					pv.invalidate();
				}
				break;
		}
	}

	PaintView findPaintViewByIpAddress(String ip){
		PaintView paintView = null;
		for (int i = 0; i < paintViews.size(); i++){
			if(paintViews.get(i).ipAddress.equals(ip)){
				paintView = paintViews.get(i);
				return paintView;
			}
		}
		Log.i("CID", "could not find paintview");
		return paintView;
	}

	private void RemotePrintOnScreen(int x, int y){
		UDP_Client udpClient = new UDP_Client();

		/*for (int i = 1; i < connectedAddresses.size(); i++) {
			try {

				//udpClient.addr = ipTargetAddress;
				udpClient.address = connectedAddresses.get(i);
				udpClient.port = port;
				udpClient.Message = "printOnScreen," + x + "," + y;
				udpClient.Send();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/

		sendToAll(/*udpClient,*/ "printOnScreen," + x + "," + y);
	}

	private void remoteDrawOnScren(String event, int page, float x, float y, int strokeWidth, int color, boolean isLineTrail){
		UDP_Client udpClient = new UDP_Client();

		/*or (int i = 1; i < connectedAddresses.size(); i++) {
			try {
				//udpClient.addr = InetAddress.getByName(connectedAddresses.get(i));  //ipTargetAddress;
				udpClient.address = connectedAddresses.get(i);
				udpClient.port = port;
				udpClient.Message = "drawOnScreen," + event + "," + page + "," + x + "," + y + "," + strokeWidth + "," + color + "," + isLineTrail;
				udpClient.Send();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/

		sendToAll(/*udpClient,*/ "drawOnScreen," + event + "," + page + "," + x + "," + y + "," + strokeWidth + "," + color + "," + isLineTrail);

	}

	private void remoteUndoLastEdit(int pageNumber){
		UDP_Client udpClient = new UDP_Client();

		/*for (int i = 1; i < connectedAddresses.size(); i++) {
			try {
				//udpClient.addr = InetAddress.getByName(connectedAddresses.get(i));  //ipTargetAddress;
				udpClient.address = connectedAddresses.get(i);
				udpClient.port = port;
				udpClient.Message = "undo," + pageNumber;
				udpClient.Send();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/

		sendToAll(/*udpClient,*/ "undo," + pageNumber);
	}

	private void sendToAll(/*UDP_Client udpClient,*/ String message){

		for (int i = 1; i < connectedAddresses.size(); i++) {
			try {
				UDP_Client udpClient = new UDP_Client();
				//udpClient.addr = InetAddress.getByName(connectedAddresses.get(i));  //ipTargetAddress;
				udpClient.address = connectedAddresses.get(i);
				Log.i("CID", "Trying to send message to " + connectedAddresses.get(i));
				udpClient.port = port;
				udpClient.Message = message;
				udpClient.Send();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.layout_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public void createLocalGraphics(InetAddress ip){
		PaintView pv = new PaintView(mainContext);
		paintViews.add(pv);
		paintViews.get(0).ipAddress = ip.toString();
		FrameLayout.LayoutParams paintViewLayoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		paintViews.get(0).setLayoutParams(paintViewLayoutParams);
		pv.init(pageView.bitmapW, pageView.bitmapH, pageCount);
		pv.currentColor = color;
		pv.strokeWidth = strokeWidth;
		paintViewLayout.addView(paintViews.get(0));
		fitPaintViews();

		if(projectFileLocation != null) { //if we're loading a pre-existing session
			readAnnotationData(projectFileLocation);
		}
	}

	public void createRemoteGraphics(String ip){
		PaintView pv = new PaintView(mainContext);
		pv.ipAddress = ip;
		FrameLayout.LayoutParams paintViewLayoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		pv.setLayoutParams(paintViewLayoutParams);
		Log.i("PaintView", "FROM DOCUMENT PAGECOUNT = " + pageCount);
		pv.init(pageView.bitmapW, pageView.bitmapH, pageCount);
		paintViewLayout.addView(pv);
		paintViews.add(pv);
		fitPaintViews();
	}

	public void switchAnnotations(){
		if (!annotationsVisible){
			for (int i = 0; i < paintViews.size(); i++){
				paintViews.get(i).setVisibility(View.VISIBLE);
				hideAllButton.setBackgroundColor(Color.TRANSPARENT);
			}
		}
		else{
			for (int i = 0; i < paintViews.size(); i++) {
				paintViews.get(i).setVisibility(View.GONE);
				hideAllButton.setBackgroundColor(Color.RED);

			}
		}
		annotationsVisible = !annotationsVisible;
	}

	protected void fitPaintViews() {
		for (int i = 0; i < paintViews.size(); i++) {
			PaintView pv = paintViews.get(i);
			pv.pageViewTransform(pageView);
		}
	}

	protected boolean localInitialized = false;

	public void initializeLocalGraphics(){
		if(!localInitialized){
			//creates local graphics and initializes them (must be here in order to initialize with a known page count
			byte[] ipAddr = new byte[]{127, 0, 0, 1};
			try {
				InetAddress addr = InetAddress.getByAddress(ipAddr);
				createLocalGraphics(addr);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			localInitialized = true;


		}
	}

	protected void writeToFile(final String id, final String folderName) {

		String project_file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" ;

		//there might not be a need for a folder, everything could be saved on a single xml

		File newxmlfile = new File(project_file_path + folderName + "_project.crxml");
		try{
			newxmlfile.createNewFile();
		}catch(IOException e){
			Log.e("IOException", "exception in createNewFile() method");
		}
		//we have to bind the new file with a FileOutputStream
		FileOutputStream fileos = null;
		try{
			fileos = new FileOutputStream(newxmlfile);
		}catch(FileNotFoundException e){
			Log.e("FileNotFoundException", "can't create FileOutputStream");
		}
		//we create a XmlSerializer in order to write xml data
		XmlSerializer serializer = Xml.newSerializer();
		try {
			//we set the FileOutputStream as output for the serializer, using UTF-8 encoding
			serializer.setOutput(fileos, "UTF-8");
			//Write <?xml declaration with encoding (if encoding not null) and standalone flag (if standalone not null)
			serializer.startDocument(null, Boolean.valueOf(true));
			//set indentation option
			serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
			//start a tag called "root"
			serializer.startTag(null, "root");

			serializer.startTag(null, "appVersion");
			serializer.text(appVersion);
			serializer.endTag(null, "appVersion");

			serializer.startTag(null, "document");
			serializer.text(DocumentActivity.fileLocation);
			serializer.endTag(null, "document");


			serializer.startTag(null, "annotation");
			for (int i = 0; i < paintViews.size(); i++) {

				serializer.startTag(null, "user");
				serializer.startTag(null, "address");
				serializer.text(paintViews.get(i).ipAddress.toString());
				serializer.endTag(null, "address");

				for (int j = 0; j < paintViews.get(i).actionPages.size(); j++) {

					serializer.startTag(null, "page");
					serializer.attribute(null, "pageNumber", Integer.toString(j));

					for (int l = 0; l < paintViews.get(i).actionPages.get(j).size(); l++) {

						serializer.startTag(null, "action");

						//still needs color, size and other attributes
						serializer.text(paintViews.get(i).actionPages.get(j).get(l));
						serializer.endTag(null, "action");
						//}
					}


					serializer.endTag(null, "page");
				}
				serializer.endTag(null, "user");
			}
			serializer.endTag(null, "annotation");

			serializer.endTag(null, "root");
			serializer.endDocument();
			//write xml data into the FileOutputStream
			serializer.flush();
			//finally we close the file stream
			fileos.close();

		} catch (Exception e) {
			Log.e("Exception","error occurred while creating xml file");
		}


	}

	protected void readAnnotationData(String projectFileLocation){
		String ret = "error";
		 ArrayList<ArrayList<ArrayList<String>>> actionPages = new ArrayList<>();
		try {
			FileInputStream addressInputStream = new FileInputStream(projectFileLocation);
			ArrayList<String> addresses = XmlParser.parseSessionAddresses(addressInputStream);

			FileInputStream inputStream = new FileInputStream(projectFileLocation);
			actionPages = XmlParser.parseSessionData(inputStream); //store session data

				for (int i = 0; i < actionPages.size(); i++) {

					//instantiate with previous saved data a paintview for each previous user
					if (!connectedAddresses.contains(addresses.get(i))) {
						connectedAddresses.add(addresses.get(i));
						updateConnectionList();
						createRemoteGraphics(addresses.get(i));
					}

					//get each page for chosen user
					for (int j = 0; j < actionPages.get(i).size(); j++) { //iterate through each page of selected user in the session

						if (i == 0) {
							//local graphics
							//get each action for chosen page
							for (int l = 0; l < actionPages.get(i).get(j).size(); l++) { //iterate through each action of selected page (item 0 is always Local paintview)
								String[] parsedActionMessage = parseAction(actionPages.get(i).get(j).get(l));

								String action = parsedActionMessage[0];
								int pageNumber = Integer.parseInt(parsedActionMessage[1]);
								float x = Float.parseFloat(parsedActionMessage[2]);
								float y = Float.parseFloat(parsedActionMessage[3]);
								int strokeWidth = Integer.parseInt(parsedActionMessage[4]);
								int color = Integer.parseInt(parsedActionMessage[5]);
								boolean isTrail = Boolean.parseBoolean(parsedActionMessage[6]);

								drawOnScreenLocal(action, j, x, y, strokeWidth, color, isTrail);

							}
						} else { //remote graphics
							//get each action for chosen page
							for (int l = 0; l < actionPages.get(i).get(j).size(); l++) { //iterate through each action of selected page (all other items in list are always remote paintviews)
								String[] parsedActionMessage = parseAction(actionPages.get(i).get(j).get(l));

								String action = parsedActionMessage[0];
								int pageNumber = Integer.parseInt(parsedActionMessage[1]);
								float x = Float.parseFloat(parsedActionMessage[2]);
								float y = Float.parseFloat(parsedActionMessage[3]);
								int strokeWidth = Integer.parseInt(parsedActionMessage[4]);
								int color = Integer.parseInt(parsedActionMessage[5]);
								boolean isTrail = Boolean.parseBoolean(parsedActionMessage[6]);

								drawOnScreenRemote(addresses.get(i),action, j, x, y, strokeWidth, color, isTrail);
							}
						}

					}
				}

		}
		catch (FileNotFoundException e) {
			Log.e("login activity", "File not found: " + e.toString());
		} catch (IOException e) {
			Log.e("login activity", "Can not read file: " + e.toString());
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}

	}
	public static String[] parseAction(String actionMessage){
		String[] splitMessage;
		splitMessage = actionMessage.split(",");
		String _temp = "";

		for (int i = 0; i < splitMessage.length; i++){
			_temp += splitMessage[i] + " ";
		}

		return splitMessage;
	}

}
