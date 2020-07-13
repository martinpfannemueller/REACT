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
import java.util.UUID;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Updated;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.log4j.PropertyConfigurator;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.zeroc.Ice.Current;
import com.zeroc.Ice.ObjectPrx;

import Manta.MAPECommunication.IALElementPrx;
import Manta.MAPECommunication.StringKnowledgeRecord;
import Manta.Sensing.ISensor;
import structure.logic.InformationCategory;
import util.Net;

@Component
public class Sensor implements ISensor {
	
	@Property(name = "name")
	private String name;
	
	@Property(name = "consulHost", mandatory = false)
	private String consulHost;
	
	@Property(name = "port", mandatory = false)
	private Integer port;
	
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
	private IALElementPrx successorProxy;

	private String uuid;
	private ConsulClient consulClient;
	private JmDNS jmdns;
	
	private boolean retryProxy = false;
	private Thread retryProxyThread;
	
	private long startTime = System.currentTimeMillis();
	
	public Sensor() {
		
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
		this.uuid = UUID.randomUUID().toString();
		
		this.setup();
	}
	
	private void setup() {
		if(port == null) {
			port = Net.findFreePort();
		}
		
		com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(name, "default -p " + this.port);
        com.zeroc.Ice.Object object = this;
        adapter.add(object, com.zeroc.Ice.Util.stringToIdentity(name));
        adapter.activate();
		
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
	        if(consulHost != null) {
	        	try {
	        		consulClient = new ConsulClient(this.consulHost);
	        		NewService newService = new NewService();
	                newService.setId(this.uuid);
	                newService.setName(name);
	                newService.setPort(this.port);
	                this.consulClient.agentServiceRegister(newService);
	                
	                if(this.successorName != null && this.successorName.length() > 0) {
	                	final String successorEndpoint = Net.getEndpointForName(this.consulClient, this.successorName);
	                	boolean successfullySet = this.setSuccessorProxy(successorEndpoint);
	                	if(!successfullySet) {
	                		retryProxy = true;
	                		
	                		retryProxyThread = new Thread() {
	                		    public void run() {
	                		        while(retryProxy) {
	                		        	
	                		        	boolean success = setSuccessorProxy(successorEndpoint);
	                		        	
	                		        	if(success) {
	                		        		retryProxy = false;
	                		        	}
	                		        	
	                		        	try {
											Thread.sleep(2000);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
	                		        }
	                		    }
	                		};
	                		retryProxyThread.start();
	                	}
	                }
	        	}catch(Exception e) {
	        		this.setupJmDNS();
	        	}
	        }else {
	        	this.setupJmDNS();
	        }
		}
	}
	
	@Invalidate
	public void invalidate() {
		if(consulHost != null) {
			consulClient.agentServiceDeregister(this.uuid);
		}
	}
	
	@Updated
	public void updated() {
		if(System.currentTimeMillis() - this.startTime > 10000) { // Skip updated notification on startup for 10 seconds
			this.setup();
		}
	}
	
	private void setupJmDNS() {
		System.out.println(name + " running with multicast DNS on port " + this.port);
		
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

            // Register service
            ServiceInfo serviceInfo = ServiceInfo.create("_manta._tcp.local.", this.name, this.port, "type=Sensor");
            this.jmdns.registerService(serviceInfo);
            
            // Add a service listener
            this.jmdns.addServiceListener("_manta._tcp.local.", new MonitorServiceListener(this));
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
				this.successorProxy = Manta.MAPECommunication.IALElementPrx.checkedCast(base);
				if(successorProxy == null) {
					return false;
				}
			}catch(Exception e) {
				return false;
			}
			return true;
    	}
    	return false;
	}
	
	public String getSuccessorName() {
		return this.successorName;
	}

	@Override
	public void receiveSensorData(String sensorData, Current current) {
		if(successorProxy != null) {
			new Thread() {
				public void run() {
					StringKnowledgeRecord record = new StringKnowledgeRecord();
					record.data = sensorData;
					record.category = InformationCategory.SENSOR.toString();
					record.ownerID = uuid;
					record.timeStamp = System.currentTimeMillis();
					
					if(printSendingTime) {
						System.out.println(this.getName());
						System.out.println(System.nanoTime());
					}
					successorProxy.callLogic(record);
				}
			}.start();
		}else {
			throw new IllegalStateException("No monitor set for sensor " + this.name);
		}
	}
	
	public String getName() {
		return this.name;
	}
}
