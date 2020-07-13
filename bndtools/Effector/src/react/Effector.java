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

import Manta.Effecting.ComponentChange;
import Manta.Effecting.ManagedResource;
import Manta.Effecting.Parameter;
import Manta.Effecting.ParameterChange;
import util.Net;

@Component
public class Effector implements ManagedResource {
	
	@Property(name = "name")
	private String name;
	
	@Property(name = "consulHost", mandatory = false)
	private String consulHost;
	
	@Property(name = "port", mandatory = false)
	private Integer port;
	
	private final com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize();

	private String uuid;
	private ConsulClient consulClient;
	private JmDNS jmdns;
	
	private long startTime = System.currentTimeMillis();
	
	public Effector() {
		
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
		       
        if(consulHost != null) {
        	try {
        		consulClient = new ConsulClient(this.consulHost);
        		NewService newService = new NewService();
                newService.setId(this.uuid);
                newService.setName(name);
                newService.setPort(this.port);
                this.consulClient.agentServiceRegister(newService);
        	}catch(Exception e) {
        		this.setupJmDNS();
        	}
        }else {
        	this.setupJmDNS();
        }
	}
	
	@Invalidate
	public void invalidate() {
		if(consulHost != null) {
			consulClient.agentServiceDeregister(this.uuid);
		}
	}
	
	private void setupJmDNS() {
		System.out.println(name + " running without Multicast DNS on port " + this.port);
		
		try {
            // Create a JmDNS instance
            this.jmdns = JmDNS.create(InetAddress.getLocalHost());

            // Register service
            ServiceInfo serviceInfo = ServiceInfo.create("_manta._tcp.local.", this.name, this.port, "type=Effector");
            this.jmdns.registerService(serviceInfo);
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
		
	}
	
	@Updated
	public void updated() {
		if(System.currentTimeMillis() - this.startTime > 10000) { // Skip updated notification on startup for 10 seconds
			this.setup();
		}
	}
	
	@Override
	public void sendComponentChanges(ComponentChange arg0, Current arg1) {
		for(Manta.Effecting.Component c : arg0.components) {
			System.out.println(c.className);
			for(Parameter p :c.parameters) {
				System.out.println(p.key + " : " + p.value);
			}
			System.out.print("\n");
		}
	}

	@Override
	public void sendParameterChanges(ParameterChange arg0, Current arg1) {
		System.out.println(arg0.parameter.key + " : " + arg0.parameter.value);
	}

}
