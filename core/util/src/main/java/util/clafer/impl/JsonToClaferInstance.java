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

package util.clafer.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.stringtemplate.v4.ST;

import com.google.gson.internal.LinkedTreeMap;

import util.clafer.api.IJsonToClaferInstance;
import util.clafer.data.Clafer;

public class JsonToClaferInstance implements IJsonToClaferInstance{
	
	@Override
	public String createClaferInstance(Map<String, Object> map, List<Clafer> abstractClafers) {
		StringBuilder result = new StringBuilder();
		StringBuilder stringAbstracts = new StringBuilder();
		
		for(Clafer c : abstractClafers) {
			
			boolean partOfMonitoringData = false;
			String identifier = "";
			for(final String key : map.keySet()) {
				@SuppressWarnings("unchecked")
				LinkedTreeMap<String, Object> treeMap = (LinkedTreeMap<String, Object>) map.get(key);
				if(treeMap.get("type").equals(c.getName())) {
					partOfMonitoringData = true;
					identifier = key;
					break;
				}
			}
			if(!partOfMonitoringData) {
				continue;
			}
			
			for(final String key : map.keySet()) {
				@SuppressWarnings("unchecked")
				LinkedTreeMap<String, Object> treeMap = (LinkedTreeMap<String, Object>) map.get(key);
				if(treeMap.get("type").equals(c.getName())) {
					identifier = key;
					
					ST st_header = new ST("<name> : <type>\n");
					st_header.add("name", identifier);
					
					st_header.add("type", treeMap.get("type"));
					
					result.append(st_header.render());
					
					for(String treeMapKey : treeMap.keySet()) {
						if(!treeMapKey.equals("type")) {
							ST st_attribute = new ST("\t[ <key> = <value> ]\n");
							st_attribute.add("key", treeMapKey);
							
							Object o = treeMap.get(treeMapKey);
							
							if(o instanceof LinkedTreeMap) { // JSON Object
								@SuppressWarnings("unchecked")
								LinkedTreeMap<String, Object> attributeTreeMap = (LinkedTreeMap<String, Object>)o;
								
								final String type = (String)attributeTreeMap.get("type"); 
								final String value = ((String)attributeTreeMap.get("value"))
										.replace(".", "_")
										.replace(":", "__");
								
								ST st_stringAbstractKey = new ST("<type>_<value>");
								st_stringAbstractKey.add("type", type);
								st_stringAbstractKey.add("value", value);
								final String stringAbstractKey = st_stringAbstractKey.render();
								
								ST st_stringAbstract = new ST("<key> : <type>\n");
								st_stringAbstract.add("key", stringAbstractKey);
								st_stringAbstract.add("type", type);
								
								stringAbstracts.append(st_stringAbstract.render());
								
								st_attribute.add("value", stringAbstractKey);
								
							}else if(o instanceof String) {
								throw new IllegalStateException("No strings without type possible");
							}else if(o instanceof Double) {
								Double doubleValue = (Double)o;
								if(new BigDecimal(doubleValue).scale() <= 0) {
									st_attribute.add("value", doubleValue.intValue());
								}else {
									st_attribute.add("value", o);
								}
							}else{
								st_attribute.add("value", o);
							}

							result.append(st_attribute.render());
						}
					}
				}
			}
		}
		
		// Add string abstracts to result string
		result.append(stringAbstracts.toString());
		
		return result.toString();
	}

}
