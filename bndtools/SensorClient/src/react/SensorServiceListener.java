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

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

public class SensorServiceListener implements ServiceListener {

	private SensorClient sensorClient;
	
	public SensorServiceListener(final SensorClient sensor) {
		this.sensorClient = sensor;
	}
	
	@Override
    public void serviceAdded(ServiceEvent event) {
        //System.out.println("Service added: " + event.getInfo());
    }

    @Override
    public void serviceRemoved(ServiceEvent event) { // TODO: Handle breaking services
        //System.out.println("Service removed: " + event.getInfo());
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
    	
		final String ipAddress = event.getInfo().getInet4Address().toString().replace("/", ""); // Remove leading slash
        final String endpoint = event.getName() + ":default -p " + event.getInfo().getPort() + " -h " + ipAddress;
        
    	if(event.getInfo().getPropertyString("type").equals("Sensor") && event.getName().equals(this.sensorClient.getSuccessorName())) {
    		boolean success = this.sensorClient.setSuccessorProxy(endpoint);
    		while(!success) {
    			try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    			success = this.sensorClient.setSuccessorProxy(endpoint);
    		}
    		
    	}
    	
    	
    }

}