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

import java.lang.reflect.Type;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Current;
import com.zeroc.Ice.ObjectPrx;

import Manta.Effecting.ComponentChange;
import Manta.Effecting.ManagedResourcePrx;
import Manta.Effecting.Parameter;
import Manta.Effecting.ParameterChange;
import Manta.Knowledge.IKnowledgePrx;
import Manta.MAPECommunication.EffectingKnowledgeRecord;
import Manta.MAPECommunication.IALElement;
import Manta.MAPECommunication.IALElementPrx;
import Manta.MAPECommunication.KnowledgeRecord;
import Manta.MAPECommunication.StringKnowledgeRecord;
import structure.logic.ILogic;
import structure.logic.ILogicContainer;
import structure.logic.InformationType;
import util.Net;

@Component
public class ALElement implements IALElement, ILogicContainer {

	private final Type JSONTYPE = new TypeToken<Map<String, String>>() {}.getType();
	
	private Communicator communicator;
	private ILogic logic;
	private IALElementPrx successorProxy;
	private ManagedResourcePrx managedResourceProxy;
	private IKnowledgePrx knowledgeProxy;

	@Property(name = "name", mandatory = true)
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

	@Property(name = "knowledgeName", mandatory = false)
	private String knowledgeName;

	@Property(name = "knowledgeIP", mandatory = false)
	private String knowledgeIP;
	
	@Property(name = "knowledgePort", mandatory = false)
	private Integer knowledgePort;
	
	@Property(name = "logicType", mandatory = true)
	private String logicType;
	
	@Property(name = "logicBundle")
	private String logicBundle;
	
	@Property(name = "logicClass")
	private String logicClass;
	
	@Property(name = "networkInterface", mandatory = false)
	private String networkInterface;
	
	@Property(name = "clafer_exec_path", mandatory = false)
	private String clafer_exec_path;
	
	@Property(name = "monitoringStrategy", mandatory = false)
	private String monitoringStrategy;
	
	@Property(name = "logInputOutput", mandatory = false)
	private boolean logInputOutput;

	@Property(name = "printSendingTime", mandatory = false)
	private boolean printSendingTime;
	
	private String uuid;

	private ConsulClient consulClient;
	private JmDNS jmdns;

	private Gson gson;
	
	private boolean retryProxyFinished = false;
	private Thread retryProxyThread;
	
	private long startTime = System.currentTimeMillis();
	
	private BundleContext context;

	public ALElement(BundleContext context) {

		// Setup log4j
		Properties properties = new Properties();
		properties.setProperty("log4j.rootLogger", "ERROR, stdout");
		properties.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
		properties.setProperty("log4j.appender.stdout.Target", "System.out");
		properties.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
		properties.setProperty("log4j.appender.stdout.layout.ConversionPattern",
				"%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n");
		PropertyConfigurator.configure(properties);

		this.context = context;
		
		com.zeroc.Ice.InitializationData initData = new com.zeroc.Ice.InitializationData();
		initData.classLoader = context.getBundle().adapt(BundleWiring.class).getClassLoader();
		this.communicator = com.zeroc.Ice.Util.initialize(initData);
		
		this.gson = new Gson();
	}

	@Validate
	public void validate() {
		this.uuid = UUID.randomUUID().toString();
		
		this.setup();
	}
	
	@Updated
	public void updated() {
		if(System.currentTimeMillis() - this.startTime > 10000) { // Skip updated notification on startup for 10 seconds
			this.setup();
		}
	}

	@Invalidate
	public void invalidate() {
		if (consulHost != null) {
			consulClient.agentServiceDeregister(this.uuid);
		}
	}
	
	private void setup() {
		if (port == null) {
			port = Net.findFreePort();
		}

		com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(name, "default -p " + this.port);
		com.zeroc.Ice.Object object = this;
		adapter.add(object, com.zeroc.Ice.Util.stringToIdentity(name));
		adapter.activate();

		if(successorIP != null && !successorIP.isEmpty() && successorPort != null 
				&& ( (knowledgeIP != null && !knowledgeIP.isEmpty() && knowledgePort != null) || this.logicType.equals("Monitor") ) ) { // In case the successor and knowledge IP and port are set manually
			
			if(!this.logicType.equals("Monitor")) {
				final String knowledgeEndpoint = this.knowledgeName + ":default -p " + this.knowledgePort + " -h " + this.knowledgeIP;
				this.instantiate(knowledgeEndpoint);
			}else {
				this.instantiateLogic(null);
			}
			
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
		}else { // Otherwise use Consul registry or multicast DNS
			if (consulHost != null) {
				try {
					consulClient = new ConsulClient(this.consulHost);
					NewService newService = new NewService();
					newService.setId(this.uuid);
					newService.setName(name);
					newService.setPort(this.port);
					this.consulClient.agentServiceRegister(newService);

					if (this.successorName != null && this.successorName.length() > 0) {
						final String successorEndpoint = Net.getEndpointForName(this.consulClient, this.successorName);
						boolean successfullySet = this.setSuccessorProxy(successorEndpoint);
						if (!successfullySet) {
							retryProxyThread = new Thread() {
								public void run() {
									while (!retryProxyFinished) {

										retryProxyFinished = setSuccessorProxy(successorEndpoint);

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
					}else {
						System.out.println("Successor name is null for " + this.name);
					}

					final String knowledgeEndpoint = Net.getEndpointForName(consulClient, knowledgeName);
					this.instantiate(knowledgeEndpoint);

				} catch (Exception e) {
					this.setupJmDNS();
				}
			} else {
				this.setupJmDNS();
			}
		}
		
		if (clafer_exec_path != null && !clafer_exec_path.isEmpty()) {
			try {
				this.updateEnv("CLAFER_EXEC_PATH", clafer_exec_path);
			} catch (ReflectiveOperationException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void setupJmDNS() {
		System.out.println(name + " running with Multicast DNS on port " + this.port);
		
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
            ServiceInfo serviceInfo = ServiceInfo.create("_manta._tcp.local.", this.name, this.port, "type=ALElement");
            this.jmdns.registerService(serviceInfo);
            
            // Add a service listener
            this.jmdns.addServiceListener("_manta._tcp.local.", new MantaServiceListener(this));
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
		
	}
	
	public void instantiate(final String knowledgeEndpoint) {
		new Thread() {
			public void run() {
				do {
				    try {
				    	ObjectPrx base = communicator.stringToProxy(knowledgeEndpoint);
				    	knowledgeProxy = Manta.Knowledge.IKnowledgePrx.checkedCast(base);
				    	instantiateLogic(knowledgeProxy);
				    	System.out.println(name + " connected to knowledge with endpoint " + knowledgeEndpoint);
				        break;
				    }
				    catch (Exception e) {
				    	try {
							Thread.sleep(1000);
						} catch (InterruptedException interruptedException) {
							interruptedException.printStackTrace();
						}
				    }
				} while(true);
			}
		}.start();
	}
	
	public void instantiateLogic(final IKnowledgePrx knowledgeProxy) {
		if (knowledgeProxy == null && !this.logicType.equals("Monitor")) {
			throw new Error("Invalid knowledge proxy");
		}

		if (this.logicType != null) {

			HashMap<String, String> properties = new HashMap<>();
			properties.put("monitoringStrategy", this.monitoringStrategy);
			
			if(this.logicBundle == null) { // Set default logicBundle --> Clafer
				if(this.logicType.equals("Monitor")) {
					this.logicBundle = "react.clafermonitor";
				}else if(this.logicType.equals("Analyzer")) {
					this.logicBundle = "react.claferanalyzer";
				}else if(this.logicType.equals("Planner")) {
					this.logicBundle = "react.claferplanner";
				}else if(this.logicType.equals("Executor")) {
					this.logicBundle = "react.claferexecutor";
				}
			}
			
			Bundle[] bundles = context.getBundles();
			Bundle bundle = null;
			for(Bundle b : bundles) {
				if(b.getSymbolicName().equals(this.logicBundle)) {
					bundle = b;
					break;
				}
			}
			if(bundle == null) {
				throw new IllegalStateException("Logic class bundle named " + this.logicBundle + " could not be found");
			}
			
			if(this.logicClass == null) { // Set default logicClass --> Clafer
				if(this.logicType.equals("Monitor")) {
					this.logicClass = "logicElements.monitor.ClaferMonitor";
				}else if(this.logicType.equals("Analyzer")) {
					this.logicClass = "logicElements.analyzer.ClaferAnalyzer";
				}else if(this.logicType.equals("Planner")) {
					this.logicClass = "logicElements.planner.ClaferPlanner";
				}else if(this.logicType.equals("Executor")) {
					this.logicClass = "logicElements.executor.ClaferExecutor";
				}
			}

			if (this.logicType.equals("Monitor")) {
				ILogic logic = getLogicInstance(bundle, properties);
				this.implementLogic(logic);
			} else if (this.logicType.equals("Analyzer")) {
				if(this.logicClass.equals("logicElements.analyzer.ClaferAnalyzer")) {
					ILogic logic = getLogicInstance(bundle, properties, true);
					this.implementLogic(logic);
				}else {
					ILogic logic = getLogicInstance(bundle, properties);
					this.implementLogic(logic);
				}
			} else if (this.logicType.equals("Planner")) {
				if(this.logicClass.equals("logicElements.planner.GPPlanner")) {
					ILogic logic = getLogicInstance(bundle, properties);
					this.implementLogic(logic);
				}else if(this.logicClass.equals("logicElements.planner.ClaferPlanner")) {
					ILogic logic = getLogicInstance(bundle, properties, true);
					this.implementLogic(logic);
				}else {
					throw new IllegalStateException("Invalid logic class for planner");
				}
			} else if (this.logicType.equals("Executor")) {
				if(this.logicClass.equals("logicElements.executor.ClaferExecutor")) {
					ILogic logic = getLogicInstance(bundle, properties, true);
					this.implementLogic(logic);
				}else {
					ILogic logic = getLogicInstance(bundle, properties);
					this.implementLogic(logic);
				}
			}
			System.out.println("Running " + this.logicType + " with logic class " + this.logicClass + " and bundle " + this.logicBundle);
		}else {
			System.out.println("logicType attribute must be provided!");
		}
	}
	
	private ILogic getLogicInstance(final Bundle bundle, final HashMap<String, String> properties) {
		return getLogicInstance(bundle, properties, false);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ILogic getLogicInstance(final Bundle bundle, final HashMap<String, String> properties, final boolean needsKnowledgeProxy) {
		if(needsKnowledgeProxy) {
			Class cl = null;
			try {
				cl = bundle.loadClass(this.logicClass);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new IllegalStateException("Logic class not found");
			}
			
			Constructor con = null;
			try {
				con = cl.getConstructor(IKnowledgePrx.class);
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				throw new IllegalStateException("Constructor of logic class not found");
			}
			try {
				if(knowledgeProxy == null) {
					throw new IllegalStateException("knowledgeProxy cannot be null for logic class " + this.logicClass);
				}
				ILogic logic = (ILogic) con.newInstance(knowledgeProxy);
				logic.initializeLogic(properties);
				return logic;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace();
				throw new IllegalStateException("Logic class could not be instantiated");
			}
		}else {
			try {
				ILogic logic = (ILogic) bundle.loadClass(this.logicClass).newInstance();
				logic.initializeLogic(properties);
				return logic;
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				e.printStackTrace();
				throw new IllegalStateException("Logic class could not be instantiated");
			}
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	public void updateEnv(String name, String val) throws ReflectiveOperationException {
		Map<String, String> env = System.getenv();
		Field field = env.getClass().getDeclaredField("m");
		field.setAccessible(true);
		((Map<String, String>) field.get(env)).put(name, val);
	}

	public boolean setSuccessorProxy(final String successorEndpoint) {
		if (successorEndpoint != null && successorEndpoint.length() > 0) {
			try {
				ObjectPrx base = communicator.stringToProxy(successorEndpoint);
				if (!successorEndpoint.contains("Effector")) {
					successorProxy = Manta.MAPECommunication.IALElementPrx.checkedCast(base);
					if(successorProxy == null) {
						return false;
					}
				} else {
					managedResourceProxy = Manta.Effecting.ManagedResourcePrx.checkedCast(base);
					if(managedResourceProxy == null) {
						return false;
					}
				}
			}catch(Exception e) {
				return false;
			}
			return true;
		}

		return false;
	}
	
	public boolean hasSuccessorProxy() {
		if(this.successorProxy != null || this.managedResourceProxy != null) {
			return true;
		}
		return false;
	}

	public boolean hasKnowledgeProxy() {
		if(this.knowledgeProxy != null) {
			return true;
		}
		return false;
	}

	public void implementLogic(ILogic logic) {
		this.logic = logic;
		logic.setContainer(this);
	}

	@Override
	public void callLogic(KnowledgeRecord record, Current current) {
		if(logInputOutput && record instanceof StringKnowledgeRecord) {
			StringKnowledgeRecord stringKnowledgeRecord = (StringKnowledgeRecord)record;
			System.out.println(this.getName() + " received " + stringKnowledgeRecord.data);
		}
		this.logic.callLogic(record);
	}

	@Override
	public void setSuccessor(String successorString, Current current) {
		this.successorProxy = (IALElementPrx) this.communicator.stringToProxy(successorString);
	}
	
	public String getSuccessorName() {
		return this.successorName;
	}

	@Override
	public void prepareDataForSending(KnowledgeRecord record, InformationType dataType) {

		if (!dataType.toString().equals(InformationType.Executing().toString())) {

			if (!(record instanceof StringKnowledgeRecord)) {
				throw new IllegalStateException("Not a String knowledge record");
			}else {
				if(logInputOutput) {
					StringKnowledgeRecord stringKnowledgeRecord = (StringKnowledgeRecord)record;
					System.out.println(this.getName() + " sends " + stringKnowledgeRecord.data);
				}
			}

			if (this.successorProxy != null) {
				if(this.printSendingTime) {
					System.out.println(this.getName());
					System.out.println(System.nanoTime());
				}
				this.successorProxy.callLogic(record);
			}else {
				System.out.println("Successor is not set for " + this.name);
			}
		} else {
			if(record instanceof StringKnowledgeRecord) {
				final String dataString = ((StringKnowledgeRecord)record).data;
				
				ComponentChange c = new ComponentChange();
				List<Manta.Effecting.Component> components = new ArrayList<>();
				
				Manta.Effecting.Component component = new Manta.Effecting.Component();
				component.className = "System";
				
				final Map<String, Object> executorData = gson.fromJson(dataString, JSONTYPE);
				List<Parameter> parameters = new ArrayList<>();
				
				for(String key : executorData.keySet()) {
					Object o = executorData.get(key);
					Parameter p = new Parameter();
					p.key = key;
					p.value = o.toString();
					parameters.add(p);
				}
				
				component.parameters = parameters.toArray(new Parameter[parameters.size()]);
				components.add(component);
				c.components = components.toArray(new Manta.Effecting.Component[components.size()]);
				
				if (this.managedResourceProxy != null) {
					if(this.printSendingTime) {
						System.out.println(this.getName());
						System.out.println(System.nanoTime());
						System.out.println("Components: " + c.components.length);
					}
					this.managedResourceProxy.sendComponentChanges(c);
				}else {
					System.out.println("Managed resource is not set for " + this.name);
				}
				
			}else if(record instanceof EffectingKnowledgeRecord){
				final ComponentChange componentChanges = ((EffectingKnowledgeRecord)record).components;
				final Parameter[] parameterChanges = ((EffectingKnowledgeRecord)record).parameterChanges;
				
				if (this.managedResourceProxy != null) {
					if(this.printSendingTime) {
						System.out.println(this.getName());
						System.out.println(System.nanoTime());
						System.out.println("Components: " + componentChanges.components.length);
					}
					this.managedResourceProxy.sendComponentChanges(componentChanges);
					for(Parameter p : parameterChanges) {
						ParameterChange parameterChange = new ParameterChange();
						parameterChange.parameter = p;
						this.managedResourceProxy.sendParameterChanges(parameterChange);
					}
				}else {
					System.out.println("Managed resource is not set for " + this.name);
				}
			}else {
				throw new IllegalStateException("Unknown KnowledgeRecord");
			}
		}
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	public String getKnowledgeName() {
		return this.knowledgeName;
	}
	
	public String getLogicType() {
		return logicType;
	}

}
