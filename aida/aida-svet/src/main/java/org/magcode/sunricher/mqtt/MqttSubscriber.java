package org.magcode.sunricher.mqtt;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.sunricher.wifi.api.ColorHandler;

import java.util.ArrayList;

public class MqttSubscriber implements MqttCallback {
	private static final String BRIGHT = "brightness";
	private static final String POW = "power";
	private static final String AT = "at";
	private static ColorHandler ledHandler;
	private UDPClient updClient;
	private static Logger logger = LogManager.getLogger(MqttSubscriber.class);
	
	public MqttSubscriber(ColorHandler aLedHandler) {
		ledHandler = aLedHandler;
	}

	public MqttSubscriber(ColorHandler aLedHandler, UDPClient anUdpClient) {
		ledHandler = aLedHandler;
		updClient = anUdpClient;
	}

	public void connectionLost(Throwable throwable) {
	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		// so far implemented:
		// .../1/brightness
		// .../1,2/power payload "0" or "1" or "ON" or "OFF"

		String[] tops = topic.split("/");
		String channel = tops[tops.length - 2];

		ArrayList<Integer> zonesA = new ArrayList<Integer>();
		String[] zones = channel.toString().split(",");
		for (String aZone : zones) {
			int value = new Integer(aZone);
			zonesA.add(value);
		}

		switch (tops[tops.length - 1]) {
		case BRIGHT:
			int value = new Integer(message.toString());
			value = new Double(value * 2.55).intValue();
//			ledHandler.setBrightness(zonesA, value);
			break;
		case POW:
			value = 0;
			if ("ON".equals(message.toString())) {
				value = 1;
			} else if ("OFF".equals(message.toString())) {
				value = 0;
			} else if (StringUtils.isNumeric(message.toString())) {
				value = new Integer(message.toString());
			}
			ledHandler.togglePower(zonesA, value == 1);
			break;
		case AT:
			updClient.send(message.toString());
			break;
		}
		logger.info("Received MQTT message via topic {}: {}", topic, message.toString());		
	}

	public void deliveryComplete(IMqttDeliveryToken t) {
	}

}