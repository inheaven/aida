package org.magcode.sunricher.mqtt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TcpClient {
	private ScheduledExecutorService executor;
	private OutputStream outputStream = null;
	private Socket socket = null;
	private static final int keepAliveSeconds = 280;
	private String host;
	private int port;
	ScheduledFuture<?> future;
	private boolean connecctionInProgress = false;
	private static Logger logger = LogManager.getLogger(TcpClient.class);
	private UDPClient udpClient;

	public TcpClient(String aHost, int aPort) {
		this.host = aHost;
		this.port = aPort;
	}

	public void init() {
		executor = Executors.newScheduledThreadPool(1);
		future = executor.scheduleAtFixedRate(new TcpHeartBeat(this), keepAliveSeconds, keepAliveSeconds,
				TimeUnit.SECONDS);
		connect();
	}

	public void shutDown() {
		future.cancel(true);
		disconnect();
	}

	public void connect() {

		if (connecctionInProgress) {
			logger.info(
					"Reconnecting to LED Controller will be ommitted because another process is trying to connect.");
			return;
		}

		connecctionInProgress = true;

		// close everything in case it was already connected
		disconnect();
		boolean connected = false;
		logger.info("Connecting to LED Controller...");

		while (connected == false) {
			try {

				InetSocketAddress addr = new InetSocketAddress(host, port);
				socket = new Socket();
				socket.connect(addr);
				socket.setSendBufferSize(8);
				socket.setReceiveBufferSize(8);
				socket.setSoTimeout(1000);
				outputStream = socket.getOutputStream();
				Thread.sleep(300);
				logger.info("Connected to LED Controller");
				connected = true;
				connecctionInProgress = false;

				try {
					InputStream inputS = socket.getInputStream();
					int read;
					while((read = inputS.read()) != -1) {
						System.out.print(read + " ");
					}
					System.out.println();

				} catch (SocketTimeoutException e) {
				}
			} catch (UnknownHostException e) {
				logger.error("Host unknown. Connection failed.", e);
				return;
			} catch (InterruptedException | IOException e) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					// we cannot do much here.
				}
			}
		}
	}

	private void disconnect() {
		if (outputStream != null) {
			try {
				outputStream.close();
				outputStream = null;
				socket.close();
				socket = null;
				logger.info("Disconnected from LED Controller");
			} catch (IOException e) {
				logger.error("Error during disconnected", e);
			}
		}
	}

	public Socket getSocket() {
		return socket;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public UDPClient getUpdClient() {
		return udpClient;
	}

	public void setUpdClient(UDPClient updClient) {
		this.udpClient = updClient;
	}
}
