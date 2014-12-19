package com.luk.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("ClickableViewAccessibility")
public class Main extends Activity implements SensorEventListener {
	private boolean LIGHT = false;
	private boolean SOUND = false;
	
	// kod przekazania adresu urzadzenia
	private static final int GET_DEVICE = 1;
	private Random generator = new Random();
	
	// status aplikacji
	private static final int DISCONNECTED = 1;
	private static final int CONNECTED = 2;
	private static final int CONNECTION_ERROR = 3;
	private static int STATE = 1;

	// bluetooth
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mDevice;
	private BluetoothSocket mSocket;
	private static final UUID uuid = UUID
			.fromString("00001101-0000-1000-8000-00805f9b34fb"); // uuid dla
																	// wymiany
																	// danych
	final Handler handler = new Handler();
	private ConnectedThread mConnectedThread;

	// sensory
	SensorManager sensorManager = null;

	// kontrolki
	private TextView mTextState;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.car_controller_layout);


		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth jest niedostępny!",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Toast.makeText(this, "Bluetooth jest wyłączony!", Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		}

		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		if (pairedDevices.size() == 0) {
			Toast.makeText(this, "Brak sparowanych urządzeń!",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		mTextState = (TextView) findViewById(R.id.tvState);
		setButtons();
	}

	private boolean ClickedButton(float dx, float dy,
			int clicked) {

		switch(clicked){
			case 0:{//up
				if(dx < 200.0F && dy < 200.0F)
					return true;
				else return false;
			}
			case 1:{//left
				if(dx > 700.0F && dy < 200.0F)
					return true;
				else return false;
			}
			case 2:{//down
				if(dx < 200.0F && dy > 200.0F)
					return true;
				else return false;
			}
			case 3:{//right
				if(dx >700 && dy > 200.0F)
					return true;
				else return false;
			}
			case 4:{
				if(dx>200 && dx<400 && dy<200)
					return true;
				else return false;
			}
			case 5:{
				if(dx>200 && dx<400 && dy>200)
					return true;
				else return false;
			}
			case 6:{
				if(dx>400 && dx<700 && dy<200)
					return true;
				else return false;
			}
			case 7:{
				if(dx>400 && dx<700 && dy>200)
					return true;
				else return false;
			}
		}
		return false;
	}
	private int RandomColor(){
		int dr = generator.nextInt(256);
		int dg = generator.nextInt(256);
		int db = generator.nextInt(256);
		return Color.rgb(dr, dg, db);
//		return generator.nextInt();
		
	}
	private void setButtons() {
		final RelativeLayout lay = (RelativeLayout) findViewById(R.id.Controller_Layout);
		lay.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(MotionEvent.ACTION_MASK & event.getAction()){
					case MotionEvent.ACTION_DOWN:{
						float dx = event.getX();
						float dy = event.getY();
						
						if(ClickedButton(dx, dy, 0)){//up
							if(mConnectedThread!= null)
								mConnectedThread.write(0x00);
							lay.setBackgroundColor(Color.RED);
						}
						else if(ClickedButton(dx, dy, 1)){//left
							if(mConnectedThread!= null)
								mConnectedThread.write(0x01);
							lay.setBackgroundColor(Color.BLUE);
						}
						else if(ClickedButton(dx, dy, 2)){//down
							if(mConnectedThread!= null)
								mConnectedThread.write(0x02);
							lay.setBackgroundColor(Color.YELLOW);
						}
						else if(ClickedButton(dx, dy, 3)){//right
							if(mConnectedThread!= null)
								mConnectedThread.write(0x03);
							lay.setBackgroundColor(Color.BLACK);
						}
						else if(ClickedButton(dx, dy, 4)){//sound
							if(mConnectedThread!= null)
								mConnectedThread.write(0x16);
							lay.setBackgroundColor(Color.LTGRAY);
							ImageView view = (ImageView)findViewById(R.id.HornButton);
							view.setImageResource(R.drawable.a_sound_up);
							
						}
						else if(ClickedButton(dx, dy, 5)){//light
							if(mConnectedThread!= null)
								mConnectedThread.write(0x18);
							lay.setBackgroundColor(Color.MAGENTA);
							LIGHT = (LIGHT==true)? false: true;
							ImageView view = (ImageView)findViewById(R.id.LightButton);
							if(LIGHT)
								view.setImageResource(R.drawable.light_up);
							else
								view.setImageResource(R.drawable.light_down);
						}
						else if(ClickedButton(dx, dy, 6)){//acc++
							if(mConnectedThread!= null)
								mConnectedThread.write(0x19);
							lay.setBackgroundColor(Color.CYAN + 123);
						}
						else if(ClickedButton(dx, dy, 7)){//acc--
							if(mConnectedThread!= null)
								mConnectedThread.write(0x20);
							lay.setBackgroundColor(Color.BLUE - 200);
						}
						return true;
					}
					case MotionEvent.ACTION_POINTER_DOWN:{
						float dx0=event.getX(0);
						float dy0=event.getY(0);
						float dx1=event.getX(1);
						float dy1=event.getY(1);
						//up+left
						if(ClickedButton(dx0, dy0, 0) && ClickedButton(dx1, dy1, 1) ||
								ClickedButton(dx0, dy0, 1) && ClickedButton(dx1, dy1, 0)){
							if(mConnectedThread != null)
								mConnectedThread.write(0x09);
							lay.setBackgroundColor(RandomColor());
						}//up+right
						else if(ClickedButton(dx0, dy0, 0) && ClickedButton(dx1, dy1, 3) ||
								ClickedButton(dx0, dy0, 3) && ClickedButton(dx1, dy1, 0)){
							if(mConnectedThread != null)
								mConnectedThread.write(0x08);
							lay.setBackgroundColor(RandomColor());
						}//down+right
						else if(ClickedButton(dx0, dy0, 2) && ClickedButton(dx1, dy1, 3) ||
								ClickedButton(dx0, dy0, 3) && ClickedButton(dx1, dy1, 2)){
							if(mConnectedThread != null)
								mConnectedThread.write(0x10);
							lay.setBackgroundColor(RandomColor());
						}//down+left
						else if(ClickedButton(dx0, dy0, 2) && ClickedButton(dx1, dy1, 1) ||
								ClickedButton(dx0, dy0, 1) && ClickedButton(dx1, dy1, 2)){
							if(mConnectedThread != null)
								mConnectedThread.write(0x11);
							lay.setBackgroundColor(RandomColor());
						}
						return true;
					}
					case MotionEvent.ACTION_UP:{
						if(mConnectedThread != null){
							//normal directions
							mConnectedThread.write(0x04);
							mConnectedThread.write(0x05);
							mConnectedThread.write(0x06);
							mConnectedThread.write(0x07);
							//multitouch directions
							mConnectedThread.write(0x12);
							mConnectedThread.write(0x13);
							mConnectedThread.write(0x14);
							mConnectedThread.write(0x15);
							//Sound disable
							mConnectedThread.write(0x17);
						}
						lay.setBackgroundColor(Color.DKGRAY);
						ImageView view = (ImageView)findViewById(R.id.HornButton);
						view.setImageResource(R.drawable.a_sound_down);
						
						return true;
					}
				}

				return false;
			}
		});
	}

	private class ConnectedThread extends Thread {

		private InputStream mmInStream = null;
		private OutputStream mmOutStream = null;

		public ConnectedThread(BluetoothSocket socket) {
			try {
				mmInStream = socket.getInputStream();
				mmOutStream = socket.getOutputStream();
			} catch (IOException e) {
				handler.post(new Runnable() {
					public void run() {
						btDisconnect();
						changeState(CONNECTION_ERROR);
						mConnectedThread = null;
					}
				});
			}

		}

		public void run() {

			byte[] buffer = new byte[1024];
			int bytes;

			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);

					if (bytes > 0) {
						byte[] newbuffer = new byte[bytes];

						for (int i = 0; i < bytes; i++)
							newbuffer[i] = buffer[i];

						final String data = new String(newbuffer, "US-ASCII");
						handler.post(new Runnable() {
							public void run() {
								// mTextLog.append(data);
								// mTextLog.scrollBy(0, 1);
							}
						});
					}

				} catch (IOException e) {
					Log.e("BT", "watcher", e);
					break;
				}
			}
		}

		void write(int one) {
			if (STATE != CONNECTED)
				return;

			try {
				mmOutStream.write(one);
			} catch (IOException e) {
				handler.post(new Runnable() {
					public void run() {
						btDisconnect();
						changeState(CONNECTION_ERROR);
						mConnectedThread = null;
					}
				});
			}
		}

		void write(String str) {
			if (STATE != CONNECTED)
				return;

			try {
				mmOutStream.write(str.getBytes());
			} catch (IOException e) {

				synchronized (Main.this) {
					btDisconnect();
					changeState(CONNECTION_ERROR);
					mConnectedThread = null;
				}
			}

		}
	}

	public void onSensorChanged(SensorEvent event) {
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	private void btConnect() {
		if (mDevice == null)
			return;
		try {
			mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
			mSocket.connect();

		} catch (IOException e) {
			Log.e("BT", "point1", e);

			btDisconnect();
			changeState(CONNECTION_ERROR);
			return;
		}

		mConnectedThread = new ConnectedThread(mSocket);
		mConnectedThread.start();

		changeState(CONNECTED);
	}

	private void btConnect(String address) {
		mDevice = mBluetoothAdapter.getRemoteDevice(address);
		btConnect();
	}

	private void btDisconnect() {
		if (mSocket == null)
			return;

		if (mConnectedThread != null) {
			mConnectedThread.stop();
			mConnectedThread = null;
		}

		try {
			mSocket.close();
		} catch (IOException e) {
			Log.e("BT", "point3", e);
		}

		mSocket = null;

		changeState(DISCONNECTED);
	}

	private void changeState(int iState) {

		STATE = iState;

		switch (iState) {
		case CONNECTED:
			mTextState.setTextColor(Color.BLUE);
			mTextState.setText("Połączony z " + mDevice.getName());
			break;
		case DISCONNECTED:
			mTextState.setTextColor(Color.BLACK);
			mTextState.setText("Rozłączony");
			break;
		case CONNECTION_ERROR:
			mTextState.setTextColor(Color.RED);
			mTextState.setText("Błąd połączenia");
			break;

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		menu.findItem(R.id.connect).setVisible(STATE != CONNECTED);
		menu.findItem(R.id.disconnect).setVisible(STATE == CONNECTED);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.connect:
			Intent in = new Intent(this, SelectDevice.class);
			startActivityForResult(in, GET_DEVICE);
			return true;
		case R.id.disconnect:
			btDisconnect();
			mDevice = null;
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case GET_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				btConnect(data.getStringExtra(SelectDevice.DEVICE_ADDRESS));
			}
			break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		changeState(STATE);
	}
}
