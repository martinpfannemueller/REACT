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

package util;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.ecwid.consul.v1.health.model.HealthService;
import com.zeroc.Ice.Communicator;

import Manta.Knowledge.IKnowledgePrx;

public class Net {

	private static final int RETRIES = 3;
	
	/**
	 * Returns a free port number on localhost.
	 * 
	 * Heavily inspired from org.eclipse.jdt.launching.SocketUtil (to avoid a dependency to JDT just because of this).
	 * Slightly improved with close() missing in JDT. And throws exception instead of returning -1.
	 * 
	 * @return a free port number on localhost
	 * @throws IllegalStateException if unable to find a free port
	 */
	public static int findFreePort() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(0);
			socket.setReuseAddress(true);
			int port = socket.getLocalPort();
			return port;
		} catch (IOException e) { 
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		throw new IllegalStateException("Could not find a free TCP/IP port");
	}
	
	public static String getEndpointForName(ConsulClient consulClient, String serviceName) {
		HealthServicesRequest request = HealthServicesRequest.newBuilder().setPassing(true)
				.setQueryParams(QueryParams.DEFAULT).build();
		Response<List<HealthService>> healthyServices = consulClient.getHealthServices(serviceName,
				request);
		
		int tries = 0;
		while(healthyServices.getValue().size() == 0) {
			if(tries >= RETRIES) {
				break;
			}
			
			healthyServices = consulClient.getHealthServices(serviceName, request);
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			tries = tries + 1;
		}
		
		if(healthyServices.getValue().isEmpty()) {
			return null;
		}
		
		HealthService service = healthyServices.getValue().get(0);
		
		final String endpoint = service.getService().getService() + ":default -p " + service.getService().getPort();
		return endpoint;
	}
	
	public static IKnowledgePrx getKnowledgeProxyWithEndpoint(Communicator communicator, String endpoint) {
		com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy(endpoint);
        Manta.Knowledge.IKnowledgePrx knowledgeProxy = Manta.Knowledge.IKnowledgePrx.checkedCast(base);
        
        if(knowledgeProxy == null)
        {
            throw new Error("Invalid proxy");
        }
        
        return knowledgeProxy;
	}
	
	public static IKnowledgePrx getKnowledgeProxyWithName(ConsulClient consulClient, Communicator communicator, String name) {
		final String endpoint = getEndpointForName(consulClient, name);
		
		return getKnowledgeProxyWithEndpoint(communicator, endpoint);
	}
	
}
