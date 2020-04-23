package org.magcode.sunricher.mqtt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class TcpHeartBeat implements Runnable {
	private TcpClient client;
	private static Logger logger = LogManager.getLogger(TcpHeartBeat.class);

	public TcpHeartBeat(TcpClient aClient) {
		this.client = aClient;
	}

	@Override
	public void run() {
		try {
			byte[] send_data = new byte[1];
			this.client.getOutputStream().write(send_data);
			this.client.getOutputStream().flush();
			logger.debug("Tcp keep alive");
		} catch (IOException e) {
			logger.error("No connection", e);
			logger.info("Attempting reconnect ... ");
			// connect TCP client again
			client.connect();
			// and send "Disable Wifi"
			client.getUpdClient().sendDisableWifi();
		}
	}
}