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

package util.clafer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import util.clafer.api.IJsonToClaferInstance;
import util.clafer.data.Clafer;
import util.clafer.impl.AOSParser;
import util.clafer.impl.JsonToClaferInstance;

public class JsonToClaferInstanceTest {

	private final Type stringObjectMap = new TypeToken<Map<String, Object>>() {}.getType();
	private final Gson gson = new Gson();
	
	@Test
	public void testSimple() {
		final String str = "ServerLauncher 0..1\n" + 
				"Constants 1..1\n" + 
				"    rtThreshold -> integer 1..1 = 75\n" + 
				"abstract Context 1..1\n" + 
				"    servers -> integer 1..1\n" + 
				"    maxServers -> integer 1..1\n" + 
				"    responseTime -> integer 1..1\n" + 
				"\n" + 
				"ExtraServers 0..1\n" + 
				"HighRT 0..1\n" + 
				"[ if Context.servers < Context.maxServers then one ExtraServers	else no ExtraServers\n" + 
				"  if Context.responseTime >= Constants.rtThreshold	then one HighRT	else no HighRT \n" + 
				"  if HighRT && ExtraServers then one ServerLauncher ]";
		final List<Clafer> abstractClafers = AOSParser.getAbstractClafers(str);
		
		final String jsonData = "{\"Test\": {\"type\": \"Context\", \"servers\": 1, \"maxServers\": 3, \"responseTime\": 70}}";
		Map<String, Object> sensorMap = gson.fromJson(jsonData, stringObjectMap);
		
		final IJsonToClaferInstance jsonToClaferInstance = new JsonToClaferInstance();
		
		final String clafers = jsonToClaferInstance.createClaferInstance(sensorMap, abstractClafers);
		
		assertTrue(clafers.contains("Test : Context"));
	}
	
	@Test
	public void testAdditionalData() {
		final String str = "ServerLauncher 0..1\n" + 
				"Constants 1..1\n" + 
				"    rtThreshold -> integer 1..1 = 75\n" + 
				"abstract Context 1..1\n" + 
				"    servers -> integer 1..1\n" + 
				"    maxServers -> integer 1..1\n" + 
				"    responseTime -> integer 1..1\n" + 
				"\n" + 
				"ExtraServers 0..1\n" + 
				"HighRT 0..1\n" + 
				"[ if Context.servers < Context.maxServers then one ExtraServers	else no ExtraServers\n" + 
				"  if Context.responseTime >= Constants.rtThreshold	then one HighRT	else no HighRT \n" + 
				"  if HighRT && ExtraServers then one ServerLauncher ]";
		final List<Clafer> abstractClafers = AOSParser.getAbstractClafers(str);
		
		final String jsonData = "{\"Test\": {\"type\": \"Context\", \"servers\": 1, \"maxServers\": 3, \"responseTime\": 70}, \"Test2\": {\"type\": \"AnotherType\"}}";
		Map<String, Object> sensorMap = gson.fromJson(jsonData, stringObjectMap);
		
		final IJsonToClaferInstance jsonToClaferInstance = new JsonToClaferInstance();
		
		final String clafers = jsonToClaferInstance.createClaferInstance(sensorMap, abstractClafers);
		
		assertTrue(clafers.contains("Test : Context"));
		assertFalse(clafers.contains("Test2"));
		assertFalse(clafers.contains("AnotherType"));
	}
	
}
