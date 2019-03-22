package com.artifex.mupdf.mini;

import com.artifex.mupdf.fitz.*;
import com.artifex.mupdf.fitz.android.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.view.MotionEvent;
import android.widget.EditText;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

import java.util.ArrayList;
import java.util.Stack;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import android.widget.RelativeLayout.LayoutParams;

public class DocumentActivity extends Activity
{
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
	protected View zoomButton;
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

	//My variables
	private LayoutInflater inflater;
	private View view;
	public static RelativeLayout item;
	public Context mainContext;
	public InetAddress ipTargetAddress;
	public TextView pointer;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mainContext = getApplicationContext();



		registerReceiver(broadcastReceiver, new IntentFilter("Main.MESSAGE_RECEIVED"));

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		displayDPI = metrics.densityDpi;

		//printOnScreenDebug();
		layoutSetup();

		/*final EditText ipAddressInput = new EditText(mainContext);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		layoutParams.width = 400;
		layoutParams.height = 150;
		layoutParams.topMargin = 150;
		layoutParams.leftMargin = 50;
		ipAddressInput.setLayoutParams(layoutParams);
		item.addView(ipAddressInput);

		final Button ipAddressButton = new Button(mainContext);
		RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParams1.width = 400;
		layoutParams1.height = 150;
		layoutParams1.topMargin = 150;
		layoutParams1.leftMargin = 550;
		ipAddressButton.setText("Connect");
		//ipAddressButton.setId(ipAddressButton);
		ipAddressButton.setLayoutParams(layoutParams1);
		item.addView(ipAddressButton);
		/*String test[] = RPCParse("Function,1,2,3,4");*
		Log.i("TAG", test[0]);
		Log.i("TAG", test[1]);
		Log.i("TAG", test[2]);
		Log.i("TAG", test[3]);

		ipAddressButton.setOnClickListener(
				new View.OnClickListener()
				{
					public void onClick(View view)
					{
						Log.v("Ip input is ", ipAddressInput.getText().toString());
						try {
							//this gives a fatal exception if something that is not an IP is insert
							ipTargetAddress = InetAddress.getByName(ipAddressInput.getText().toString());
							ipAddressButton.setVisibility(View.GONE);
							ipAddressInput.setVisibility(View.GONE);
						} catch (UnknownHostException e) {
							e.printStackTrace();
							Log.i("TAG", "this is not a valid IP address");
						}
					}
				}); */

		UDP_Server udpServer = new UDP_Server();
		udpServer.runUdpServer(mainContext);

        registerReceiver(broadcastReceiver, new IntentFilter("Main.MESSAGE_RECEIVED"));


		//BroadcastReceiver br;

		/*UDP_Client udp = new UDP_Client();
		udp.func();*/

		//setContentView(R.layout.document_activity);
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
		fitPage = prefs.getBoolean("fitPage", false);
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

		zoomButton = findViewById(R.id.zoom_button);
		zoomButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fitPage = !fitPage;
				loadPage();
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

	}

	public void layoutSetup(){
		inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.document_activity, null);
		item = (RelativeLayout ) view.findViewById(R.id.mainRelativeLayout);
		setContentView(item);
	}


	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		int x =/* (int)item.getX() */+ (int)event.getRawX();
		int  y =/* (int)item.getY()*/ + (int)event.getRawY();
		View v = getCurrentFocus();

		printOnScreenLocal(x, y);
		RPCprintOnScreen(x, y);

		boolean ret = super.dispatchTouchEvent(event);
		return ret;
	}
/*
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d("DEBUG","hey");

		int x = (int)event.getX();
		int y = (int)event.getY();
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_UP:
		}
		printOnScreen(x, y);

		return false;
	}*/

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
		} else {
			loadPage();
		}


	}

	protected void openDocument() {
		worker.add(new Worker.Task() {
			boolean needsPassword;
			public void work() {
				Log.i(APP, "open document");
				if (path != null)
					doc = Document.openDocument(path);
				else
					doc = Document.openDocument(buffer, mimetype);
				needsPassword = doc.needsPassword();
			}
			public void run() {
				if (needsPassword)
					askPassword(R.string.dlog_password_message);
				else
					loadDocument();
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
		if (history.empty()) {
			super.onBackPressed();
		} else {
			currentPage = history.pop();
			loadPage();
		}
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
				if (currentPage < 0 || currentPage >= pageCount)
					currentPage = 0;
				titleLabel.setText(title);
				if (isReflowable)
					layoutButton.setVisibility(View.VISIBLE);
				else
					zoomButton.setVisibility(View.VISIBLE);
				loadPage();
				loadOutline();
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
				if (flatOutline != null)
					outlineButton.setVisibility(View.VISIBLE);
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
					if (fitPage)
						ctm = AndroidDrawDevice.fitPage(page, canvasW, canvasH);
					else
						ctm = AndroidDrawDevice.fitPageWidth(page, canvasW);
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
				if (bitmap != null)
					pageView.setBitmap(bitmap, wentBack, links, hits);
				else
					pageView.setError();
				pageLabel.setText((currentPage+1) + " / " + pageCount);
				pageSeekbar.setMax(pageCount - 1);
				pageSeekbar.setProgress(pageNumber);
				wentBack = false;
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

	public void toggleUI() {
		if (navigationBar.getVisibility() == View.VISIBLE) {
			currentBar.setVisibility(View.GONE);
			navigationBar.setVisibility(View.GONE);
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
			wentBack = true;
			currentPage--;
			loadPage();
		}
	}

	public void goForwardLocal(){
			if (currentPage < pageCount - 1) {
				currentPage++;
				loadPage();
			}
	}

	public void goBackward() {
		if (currentPage > 0) {
			wentBack = true;
			currentPage --;
			loadPage();

			UDP_Client udpClient = new UDP_Client();
			udpClient.addr = ipTargetAddress;
			udpClient.Message = "goBackward";
			udpClient.Send();
		}
	}

	public void goForward() {
		if (currentPage < pageCount - 1) {
			currentPage ++;
			loadPage();

			UDP_Client udpClient = new UDP_Client();
			udpClient.addr = ipTargetAddress;
			udpClient.Message = "goForward";
			udpClient.Send();
		}
	}

	public void gotoPage(int p) {
		gotoPageLocal(p);

		UDP_Client udpClient = new UDP_Client();
		udpClient.addr = ipTargetAddress;
		udpClient.Message = "goToPage," + p;
		udpClient.Send();
	}

	public void gotoPageLocal(int p) {
		if (p >= 0 && p < pageCount && p != currentPage) {
			history.push(currentPage);
			currentPage = p;
			loadPage();
		}
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

	public String[] RPCParse(String RPCMessage){
		String[] splitMessage;
		splitMessage = RPCMessage.split(",");
		return splitMessage;
	}

	public void printMessageOnScreen(String message, int x, int y){
		TextView  tv = new TextView(this);
		tv.setText(message);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		//ViewGroup.LayoutParams layoutParams = item.getLayoutParams();
		//layoutParams.leftMargin = 1000;
		layoutParams.width = x;
		layoutParams.height = y;
		layoutParams.topMargin = y;
		layoutParams.leftMargin = x;

		//ALL THIS STUFF MUS BE IN % OR SOMETHING TO FIT ANY SCREENSIZE

		//layoutParams.topMargin = 1000;
		//layoutParams.alignWithParent = true;

		tv.setLayoutParams(layoutParams);
		item.addView(tv);
	}

	private String newString;

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// internet lost alert dialog method call from here...
			//Log.i("tag", "DIO");
			//Toast.makeText(mainContext, "DIO MESSAGE", Toast.LENGTH_LONG);
			//Intent intentReceived = getIntent();
			Bundle messagesReceived = intent.getExtras();

			if(messagesReceived == null) {
				newString = null;
			} else {
				newString = messagesReceived.getString("Main.MESSAGE_STRING");
			}



			//do stuff with received message

			String[] parsedMessage = RPCParse(newString);

			//printMessageOnScreen(newString, 200, 200);
			//Toast.makeText(context, newString, Toast.LENGTH_LONG).show();
			switch(parsedMessage[0]) {
				case "goForward":
					goForwardLocal();
					break;
				case "goBackward":
					goBackwardLocal();
					break;
				case "goToPage":
					gotoPageLocal(Integer.parseInt(parsedMessage[1]));
					break;
				case "printOnScreen":
					printOnScreenLocal(Integer.parseInt(parsedMessage[1]), Integer.parseInt(parsedMessage[2]));
					break;
				default:
					// code block
			}
		}
	};

	public void printOnScreenDebug(){
		TextView  tv = new TextView(this);
		tv.setText("Test");
		LayoutParams layoutParams=new LayoutParams(200, 300);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		tv.setLayoutParams(layoutParams);
		item.addView(tv);
	}

	public void printOnScreen(int x, int y){
		if (pointer == null){
			pointer = new TextView(this);
			pointer.setText("0");
			item.addView(pointer);

		}
		//TextView  tv = new TextView(this);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		//ViewGroup.LayoutParams layoutParams = item.getLayoutParams();
		//layoutParams.leftMargin = 1000;
		//layoutParams.width = x;
		//layoutParams.height = y;
		layoutParams.topMargin = y;
		layoutParams.leftMargin = x;

		//ALL THIS LINES MUST BE IN % OR SOMETHING TO FIT ANY SCREENSIZE

		pointer.setLayoutParams(layoutParams);
//		item.addView(pointer);
	}

	private void printOnScreenLocal(int x, int y){
		printOnScreen(x, y);
	}

	private void RPCprintOnScreen(int x, int y){
		UDP_Client udpClient = new UDP_Client();
		udpClient.addr = ipTargetAddress;
		udpClient.Message = "printOnScreen," + x + "," + y;
		udpClient.Send();
	}

}
