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

package structure.logic.monitor;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.google.gson.internal.LinkedTreeMap;

public class AggregationMonitoringStrategyTests {
	
	@SuppressWarnings("unchecked")
	@Test
	public void test1() {
		final String string1 = "{\"CoreSwitch\":{\"switchId\":{\"value\":\"of:0000000000000001\",\"type\":\"SwitchId\"},\"type\":\"Switch\"},\"DestHost\":{\"mac\":{\"value\":\"00:00:00:00:00:10\",\"type\":\"MacAddr\"},\"ipAddress\":{\"value\":\"10.0.0.100\",\"type\":\"IpAddr\"},\"port\":6,\"type\":\"DestinationHost\"},\"StreamingServer\":{\"mac\":{\"value\":\"00:00:00:00:00:10\",\"type\":\"MacAddr\"},\"ipAddress\":{\"value\":\"10.0.0.100\",\"type\":\"IpAddr\"},\"port\":6,\"type\":\"Host\"}}";
		final String string2 = "{\"DestHost\":{\"distance\":16.0,\"type\":\"DestinationHost\"}}";
		
		IMonitoringStrategy strategy = new AggregationMonitoringStrategy();
		
		Map<String, Object> result1 = strategy.handleSensorJSON(string1);
		
		assertTrue(result1.containsKey("CoreSwitch"));
		assertTrue(result1.containsKey("DestHost"));
		assertTrue(result1.containsKey("StreamingServer"));
		
		Map<String, Object> result2 = strategy.handleSensorJSON(string2);
		
		assertTrue(result2.containsKey("CoreSwitch"));
		assertTrue(result2.containsKey("DestHost"));
		assertTrue(result2.containsKey("StreamingServer"));
		
		LinkedTreeMap<String, Object> treeMap = (LinkedTreeMap<String, Object>) result2.get("DestHost");
		
		assertTrue(treeMap.containsKey("type"));
		assertTrue(treeMap.containsKey("mac"));
		assertTrue(treeMap.containsKey("ipAddress"));
		assertTrue(treeMap.containsKey("port"));
		assertTrue(treeMap.containsKey("distance"));
		
		
		
		result1 = strategy.handleSensorJSON(string1);
		
		assertTrue(result1.containsKey("CoreSwitch"));
		assertTrue(result1.containsKey("DestHost"));
		assertTrue(result1.containsKey("StreamingServer"));
		
		result2 = strategy.handleSensorJSON(string2);
		
		assertTrue(result2.containsKey("CoreSwitch"));
		assertTrue(result2.containsKey("DestHost"));
		assertTrue(result2.containsKey("StreamingServer"));
		
		treeMap = (LinkedTreeMap<String, Object>) result2.get("DestHost");
		
		assertTrue(treeMap.containsKey("type"));
		assertTrue(treeMap.containsKey("mac"));
		assertTrue(treeMap.containsKey("ipAddress"));
		assertTrue(treeMap.containsKey("port"));
		assertTrue(treeMap.containsKey("distance"));
	}
	
}
