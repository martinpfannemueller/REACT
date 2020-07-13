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

package structure.logic.monitor.windowing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.collections4.queue.CircularFifoQueue;

public class Window {     
	
	private final int windowSize;
	private final Queue<Map<String, Object>> fifo;
	
	public Window(final int windowSize) {
		if(windowSize <= 1) {
			throw new IllegalStateException("Window size must be larger than 1!");
		}
		this.windowSize = windowSize;
		fifo = new CircularFifoQueue<>(this.windowSize);
	}
	
    public Map<String, Object> getAveragedValues(final Map<String, Object> currentSensorValues){
    	
    	if(fifo.isEmpty() || windowSize - fifo.size() > 1) { // Window is empty
    		fifo.add(currentSensorValues);
    		
    		return currentSensorValues;
    	}else if(windowSize - fifo.size() == 1 || windowSize == fifo.size()){ // Only one entry is missing of window is filled
    		fifo.add(currentSensorValues);
    		
    		Map<String, Object> averagedValues = new HashMap<>();
    		Map<String, List<Double>> temporary = new HashMap<>(); // Handle values as Double
    		
    		for(Map<String, Object> sensorValues : fifo) {
    			for(final String key : sensorValues.keySet()) {
    				if(!key.equals("type")) { // Skip type
    					if(!temporary.containsKey(key)) {
        					List<Double> list = new ArrayList<>();
        					list.add((Double)sensorValues.get(key));
        					temporary.put(key, list);
        				}else {
        					List<Double> list = temporary.get(key);
        					list.add((Double)sensorValues.get(key));
        					temporary.put(key, list);
        				}
    				}
    			}
    		}
    		
    		for(final String key : currentSensorValues.keySet()) {
    			if(key.equals("type")) {
    				averagedValues.put(key, currentSensorValues.get(key));
    			}else {
    				averagedValues.put(key, getAverage(temporary.get(key)));
    			}
    		}
    		
    		return averagedValues;
    	}else {
    		throw new IllegalStateException("Invalid windowing state");
    	}
    }
    
    private Double getAverage(final List<Double> list) {
    	if(list.size() == 0) {
    		return 0.0;
    	}
    	
    	Double sum = 0.0;
    	
    	for(Double d : list) {
    		sum += d;
    	}
    	
    	return sum/list.size();
    }
}
