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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Updated;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLPackage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;

import Manta.Knowledge.CMKnowledge;
import Manta.Knowledge.ClaferKnowledge;
import knowledge.Knowledge;
import util.Net;
import util.XMLEMFConverter;

@Component
public class KnowledgeElement {
	
	@Property(name = "name")
	private String name;
	
	@Property(name = "consulHost", mandatory = false)
	private String consulHost;
	
	@Property(name = "port", mandatory = false)
	private Integer port;
	
	@Property(name = "networkInterface", mandatory = false)
	private String networkInterface;
	
	@Property(name = "claferFile", mandatory = false)
	private String claferFile;
	
	@Property(name = "componentModelFile", mandatory = false)
	private String componentModelFile;
	
	private String uuid;
	
	private final com.zeroc.Ice.Communicator communicator;
	private ConsulClient consulClient;
	private JmDNS jmdns;
	
	private final Knowledge knowledge = new Knowledge();
	private Manta.Knowledge.IKnowledgePrx knowledgeProxy;
	
	private final XMLEMFConverter converter;
	
	private ClaferKnowledge claferKnowledge;
	private CMKnowledge cmKnowledge;
	
	private long startTime = System.currentTimeMillis();
	
	public KnowledgeElement(BundleContext context) {
		
		// Setup log4j
		Properties properties = new Properties();
		properties.setProperty("log4j.rootLogger","ERROR, stdout");
		properties.setProperty("log4j.appender.stdout","org.apache.log4j.ConsoleAppender");
		properties.setProperty("log4j.appender.stdout.Target","System.out");
		properties.setProperty("log4j.appender.stdout.layout","org.apache.log4j.PatternLayout");
		properties.setProperty("log4j.appender.stdout.layout.ConversionPattern","%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n");
		PropertyConfigurator.configure(properties);
	
		com.zeroc.Ice.InitializationData initData = new com.zeroc.Ice.InitializationData();
		initData.classLoader = context.getBundle().adapt(BundleWiring.class).getClassLoader();
		communicator = com.zeroc.Ice.Util.initialize(initData);
		
		converter = new XMLEMFConverter();
		converter.addClassMapping(UMLPackage.eINSTANCE.getClass(), "uml");
	}
	
	@Validate
	public void validate() {
		this.uuid = UUID.randomUUID().toString();
		
		this.setup();
    }
	
	private void setup() {
		
        if(claferFile != null && !claferFile.isEmpty()) {
			try {
				Scanner scanner = new Scanner( new File(claferFile) );
				final String claferString = scanner.useDelimiter("\\A").next();
	        	scanner.close();
	        	
	        	final ClaferKnowledge c = new ClaferKnowledge();
	        	c.value = claferString;
	        	this.claferKnowledge = c;
	        	System.out.println("Successfully read Clafer specification: " + claferFile);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
        	
        }
        if(componentModelFile != null && !componentModelFile.isEmpty()) {
        	try {
        		final CMKnowledge c = new CMKnowledge();
				c.value = converter.getStringfromEObject(this.getCM(componentModelFile), UMLPackage.eINSTANCE);
				this.cmKnowledge = c;
				System.out.println("Successfully read Component specification: " + componentModelFile);
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			};
        	
        }
		
		if(port == null) {
			port = Net.findFreePort();
		}
		
		com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(name, "default -p " + this.port);
        com.zeroc.Ice.Object object = knowledge;
        adapter.add(object, com.zeroc.Ice.Util.stringToIdentity(name));
        adapter.activate();
		
        final String endpoint = name + ":default -p " + this.port;
        com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy(endpoint);
        knowledgeProxy = Manta.Knowledge.IKnowledgePrx.checkedCast(base);
        if(knowledgeProxy == null)
        {
        	throw new Error("Invalid proxy");
        }
        
        if(claferKnowledge != null) {
        	this.knowledgeProxy.sendKnowledge(claferKnowledge);
        }else {
        	System.out.println("No initial Clafer knowledge loaded. In case you set a path in the configuration file, check if it is correct.");
        }
        
        if(cmKnowledge != null) {
        	this.knowledgeProxy.sendKnowledge(cmKnowledge);
        }else {
        	System.out.println("No initial Component Model knowledge loaded. In case you set a path in the configuration file, check if it is correct.");
        }
        
        // Consul
        if(consulHost != null) {
        	try {
        		consulClient = new ConsulClient(this.consulHost);
                NewService newService = new NewService();
                newService.setId(this.uuid);
                newService.setName(name);
                newService.setPort(this.port);
                this.consulClient.agentServiceRegister(newService);
        	}catch(Exception e) {
        		System.out.println("Exception setting up Consul on " + name);
        		System.out.println(name + " running with multicast DNS on port " + this.port);
        		this.setupJmDNS();
        	}
        }else {
        	System.out.println(name + " running with multicast DNS on port " + this.port);
        	this.setupJmDNS();
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

            // Register service
            ServiceInfo serviceInfo = ServiceInfo.create("_manta._tcp.local.", this.name, this.port, "type=Knowledge");
            this.jmdns.registerService(serviceInfo);
        } catch (IOException e) {
            System.out.println("Exception at setting up JmDNS: " + e.getMessage());
        }
	}
	
	@Invalidate
	public void invalidate() {
		if(consulHost != null) {
			consulClient.agentServiceDeregister(this.uuid);
		}
		
		this.jmdns.unregisterAllServices();
	}
	
	@Updated
	public void updated() {
		if(System.currentTimeMillis() - this.startTime > 10000) { // Skip updated notification on startup for 10 seconds
			this.setup();
		}
	}
	
	private Model getCM(final String absolutePath) throws MalformedURLException {
		ResourceSet resourceSet = createResourceSet();
		
		@SuppressWarnings("unused")
		UMLPackage umlPackage = UMLPackage.eINSTANCE;

		// Component model
		XMIResource resourceCM = null;
		
		final URL url = new File(absolutePath).toURI().toURL();
		
		resourceCM = (XMIResource) resourceSet.createResource(URI.createURI(url.toString()));
		try {
			resourceCM.load(null);
			Model cm = (Model) resourceCM.getContents().get(0);
			return cm;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	private ResourceSet createResourceSet() {
		final ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION,
				new XMIResourceFactoryImpl());
		return rs;
	}
	
}
