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

public class MantaServiceListener implements ServiceListener {

	private ALElement alElement;
	
	public MantaServiceListener(final ALElement alElement) {
		this.alElement = alElement;
	}
	
	@Override
	public void serviceAdded(ServiceEvent event) { // Only contains added event without (resolved) service information
		//System.out.println("Service added: " + event.getInfo());
    }

    @Override
    public void serviceRemoved(ServiceEvent event) { // TODO: Handle breaking services
        //System.out.println("Service removed: " + event.getInfo());
    }
    
    @Override
    public void serviceResolved(ServiceEvent event) {
    	this.setup(event);
    }
    
    private void setup(ServiceEvent event) {
    	final String ipAddress = event.getInfo().getInet4Address().toString().replace("/", ""); // Remove leading slash
        final String endpoint = event.getName() + ":default -p " + event.getInfo().getPort() + " -h " + ipAddress;
        
    	if(!this.alElement.hasKnowledgeProxy()) {
    		if(event.getInfo().getPropertyString("type").equals("Knowledge") && event.getName().equals(this.alElement.getKnowledgeName())) {
        		// Knowledge
                this.alElement.instantiate(endpoint);
                System.out.println(this.alElement.getName() + " connected to knowledge with endpoint " + endpoint);
        	}
    	}
    	
    	if(!this.alElement.hasSuccessorProxy()) {
        	
        	if(event.getInfo().getPropertyString("type").equals("ALElement") && event.getName().equals(this.alElement.getSuccessorName())) {
        		// MAPE
        		
        		if(this.alElement.getLogicType().equals("Monitor")) { // Monitors do not have a knowledge component --> Special treatment
        			this.alElement.instantiateLogic(null);
        		}
        		
        		boolean success = this.alElement.setSuccessorProxy(endpoint);
        		while(!success) {
        			try {
    					Thread.sleep(1000);
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    				}
        			success = this.alElement.setSuccessorProxy(endpoint);
        		}
        		if(success) {
        			System.out.println(this.alElement.getName() + " connected to successor with endpoint " + endpoint);
        		}
        	}else if(event.getInfo().getPropertyString("type").equals("Effector") && event.getName().equals(this.alElement.getSuccessorName())) {
        		// Effector
        		boolean success = this.alElement.setSuccessorProxy(endpoint);
        		while(!success) {
        			try {
    					Thread.sleep(1000);
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    				}
        			success = this.alElement.setSuccessorProxy(endpoint);
        		}
        		if(success) {
        			System.out.println(this.alElement.getName() + " connected to successor with endpoint " + endpoint);
        		}
        	}
    	}
    }
}
