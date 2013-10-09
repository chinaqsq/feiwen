package com.idongci.feiwen;

import java.io.File;
import java.lang.reflect.Field;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import org.apache.http.conn.util.InetAddressUtils;

import android.app.Activity;
import android.app.Service;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView wifiStateView;
	private WifiManager wifiManager;
	private WifiInfo wifiInfo;
	private FtpServer mFtpServer;
	int port = 9310;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		wifiStateView = (TextView) findViewById(R.id.wifiStateView);
		// �õ�WifiManager�������ǲ���Wifi���ϵĸ����������Ǹı䣬��ȡ��״̬������ͨ���˶���
		wifiManager = (WifiManager) getSystemService(Service.WIFI_SERVICE);
		// ,,Context.WIFI_SERVICE,,���ֶ����ԣ�Service��Context��һ�����࣬���е�WIFI_SERVICE�Ǵ�Context�м̳������ģ�
		// �ı�WIFI״̬��
		wifiInfo = wifiManager.getConnectionInfo();
		wifiManager.setWifiEnabled(true);// true��ʾ�򿪣�false��ʾ�رգ�
		String wifiState = "δ֪";
		switch (wifiManager.getWifiState()) {
		case WifiManager.WIFI_STATE_DISABLED:
			wifiState = "wifi�ѹر�";
			break;
		case WifiManager.WIFI_STATE_DISABLING:
			wifiState = "wifi���ڹر�";
			break;
		case WifiManager.WIFI_STATE_ENABLING:
			wifiState = "wifi���ڴ�";
			break;
		case WifiManager.WIFI_STATE_ENABLED:
			wifiState = "wifi�Ѵ�";
			break;
		}
		// ��ȡ����ip
		String intToIp = intToIp(wifiInfo.getIpAddress());
		wifiStateView.setText(wifiState+"\n���ҵĵ�������ftp://" + intToIp + ":" + port + "=="
				+ wifiInfo.getMacAddress()); // �õ�WIFI�ĵ�ǰ״̬��
		startFtpServer();
	}

	/**
	 * ����ftp����
	 */
	private void startFtpServer() {
		/**
		 * ftp���񹤳�
		 */
		FtpServerFactory serverFactory = new FtpServerFactory();
		PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
		File files = new File("user.properties");
		userManagerFactory.setFile(files);
		serverFactory.setUserManager(userManagerFactory.createUserManager());
		
		ListenerFactory factory = new ListenerFactory();

		// set the port of the listener
		factory.setPort(port);
		// replace the default listener
		serverFactory.addListener("default", factory.createListener());
		// String fieldIntToIp = fieldIntToIp(wifiInfo);
		// System.out.println("fieldIntToIp������" + fieldIntToIp);
		String getLocalIpAddress = getLocalIpAddress();
		System.out.println("getLocalIpAddress������" + getLocalIpAddress);

		// start the server
		FtpServer server = serverFactory.createServer();
		this.mFtpServer = server;
		try {
			server.start();
		} catch (FtpException e) {
			e.printStackTrace();
		}

	}

	/**
	 * �õ�����IP��ַ
	 * 
	 * @return
	 */
	public String getLocalIpAddress() {
		try {
			Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces();
			while (en.hasMoreElements()) {
				NetworkInterface nif = en.nextElement();
				Enumeration<InetAddress> enumIpAddr = nif.getInetAddresses();
				while (enumIpAddr.hasMoreElements()) {
					InetAddress mInetAddress = enumIpAddr.nextElement();
					if (!mInetAddress.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(mInetAddress
									.getHostAddress())) {
						return mInetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("MyFeiGeActivity", "��ȡ����IP��ַʧ��");
		}

		return null;
	}

	/**
	 * ����ȡ��intתΪ������ip��ַ,�ο������ϵģ��޸�����
	 * 
	 * @param ipAddress
	 * @return
	 */
	private String intToIp(int ipAddress) {
		String ipString = ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff)
				+ "." + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
		return ipString;
	}

	/**
	 * ����WifiInfo�࣬�õ�ip��ַ
	 * 
	 * @param info
	 * @return
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws Exception
	 */
	private String fieldIntToIp(WifiInfo info) {
		String ipString;
		try {
			Field field = info.getClass().getDeclaredField("mIpAddress");
			field.setAccessible(true);
			ipString = (String) field.get(info);
		} catch (Exception e) {
			throw new RuntimeException();
		}
		return ipString;
	}

	// public int getIpAddress() {
	// if (wifiInfo.mIpAddress == null || mIpAddress instanceof Inet6Address)
	// return 0;
	// return NetworkUtils.inetAddressToInt(mIpAddress);
	// }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != mFtpServer) {
			mFtpServer.stop();
			mFtpServer = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
