// Copyright 2020 Martin Pfannemüller
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package react;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.jmdns.JmDNS;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Updated;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.log4j.PropertyConfigurator;

import com.zeroc.Ice.ObjectPrx;

import Manta.Sensing.ISensorPrx;

@Component
public class SensorClient {

	@Property(name = "name")
	private String name;
	
	@Property(name = "successorName", mandatory = false)
	private String successorName;
	
	@Property(name = "successorIP", mandatory = false)
	private String successorIP;
	
	@Property(name = "successorPort", mandatory = false)
	private Integer successorPort;
	
	@Property(name = "networkInterface", mandatory = false)
	private String networkInterface;
	
	@Property(name = "printSendingTime", mandatory = false)
	private boolean printSendingTime;
	
	private final com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize();
	private ISensorPrx successorProxy;

	private JmDNS jmdns;
	
	private long startTime = System.currentTimeMillis();
	
	public SensorClient() {
		
		// Setup log4j
		Properties properties = new Properties();
		properties.setProperty("log4j.rootLogger","ERROR, stdout");
		properties.setProperty("log4j.appender.stdout","org.apache.log4j.ConsoleAppender");
		properties.setProperty("log4j.appender.stdout.Target","System.out");
		properties.setProperty("log4j.appender.stdout.layout","org.apache.log4j.PatternLayout");
		properties.setProperty("log4j.appender.stdout.layout.ConversionPattern","%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n");
		PropertyConfigurator.configure(properties);
		
	}
	
	@Validate
	public void validate() {
		this.setup();
	}
	
	private void setup() {		
        if(successorIP != null && !successorIP.isEmpty() && successorPort != null) { // In case the successor IP and port are set manually
    		final String successorEndpoint = this.successorName + ":default -p " + this.successorPort + " -h " + this.successorIP;
			
			new Thread() {
				public void run() {
					boolean success = setSuccessorProxy(successorEndpoint);
					while(!success) {
		    			try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
		    			success = setSuccessorProxy(successorEndpoint);
		    		}
					System.out.println(name + " connected to successor with endpoint " + successorEndpoint);
				}
			}.start();
		}else {
			this.setupJmDNS();
		}
	}
	
	@Updated
	public void updated() {
		if(System.currentTimeMillis() - this.startTime > 10000) { // Skip updated notification on startup for 10 seconds
			this.setup();
		}
	}
	
	private void setupJmDNS() {		
		try {
			// Create a JmDNS instance on a predefined interface or on the first interface found
			if(this.networkInterface != null && !networkInterface.isEmpty()) {
				this.jmdns = JmDNS.create(InetAddress.getByName(this.networkInterface));
				System.out.println(name + " uses " + this.networkInterface + " as network interface");
			}else {
				InetAddress address = InetAddress.getLocalHost();
				this.jmdns = JmDNS.create(address);
				System.out.println(name + " uses " + address.getHostAddress() + " as network interface (automatic selection)");
			}
            
            // Add a service listener
            this.jmdns.addServiceListener("_manta._tcp.local.", new SensorServiceListener(this));
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
		
	}
	
	public boolean setSuccessorProxy(final String successorEndpoint) {
    	if(successorEndpoint != null && successorEndpoint.length() > 0) {
    		try {
				ObjectPrx base = this.communicator.stringToProxy(successorEndpoint);
				this.successorProxy = Manta.Sensing.ISensorPrx.checkedCast(base);
				if(successorProxy == null) {
					return false;
				}
			}catch(Exception e) {
				return false;
			}
    		
    		// Call sendDummyData in case successor is set
    		sendDummyData();
    		
			return true;
    	}
    	return false;
	}
	
	public String getSuccessorName() {
		return this.successorName;
	}
	
	public String getName() {
		return this.name;
	}
	
	private void sendDummyData() {
		new Thread() {
			public void run() {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    			// Send example data after 10 seconds
    			successorProxy.receiveSensorData("{ \"Mngr\" = {\n" + 
    					"                \"type\": \"Manager\",\n" + 
    					"                \"activeServers\": 2,\n" + 
    					"                \"maxServers\": 3,\n" + 
    					"                \"responseTime\": 80\n" + 
    					"            }"
    					+ "}");
			}
		}.start();
	}
	
}
