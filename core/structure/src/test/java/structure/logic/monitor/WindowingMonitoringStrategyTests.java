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

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import structure.logic.monitor.IMonitoringStrategy;
import structure.logic.monitor.WindowingMonitoringStrategy;
import structure.logic.monitor.windowing.Window;

public class WindowingMonitoringStrategyTests {

	@Test(expected = IllegalStateException.class)
	public void testInvalidWindowSize0() {
		Window w = new Window(0);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testInvalidWindowSize1() {
		Window w = new Window(1);
	}
	
	@Test
	public void testWindowingSensorDataHandler() {
		IMonitoringStrategy handler = new WindowingMonitoringStrategy();
		
		final String dummyData = "{\n" + 
				"	\"type\": \"Context\",\n" + 
				"	\"val1\": 1,\n" + 
				"	\"val2\": 9\n" + 
				"}";
		
		Map<String, Object> result1 = handler.handleSensorJSON(dummyData);
		
		assertEquals(1.0, result1.get("val1"));
		assertEquals(9.0, result1.get("val2"));
		
		final String dummyData2 = "{\n" + 
				"	\"type\": \"Context\",\n" + 
				"	\"val1\": 5,\n" + 
				"	\"val2\": 5\n" + 
				"}";
		
		handler.handleSensorJSON(dummyData2);
		
		final String dummyData3 = "{\n" + 
				"	\"type\": \"Context\",\n" + 
				"	\"val1\": 9,\n" + 
				"	\"val2\": 1\n" + 
				"}";
		
		Map<String, Object> result2 = handler.handleSensorJSON(dummyData3);
		
		assertEquals(5.0, result2.get("val1"));
		assertEquals(5.0, result2.get("val2"));
		
		final String dummyData4 = "{\n" + 
				"	\"type\": \"Context\",\n" + 
				"	\"val1\": 7,\n" + 
				"	\"val2\": 0\n" + 
				"}";
		
		Map<String, Object> result3 = handler.handleSensorJSON(dummyData4);
		
		assertEquals(7.0, result3.get("val1"));
		assertEquals(2.0, result3.get("val2"));
	}
	
	
}
