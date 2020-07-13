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

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import structure.logic.monitor.GsonTools.ConflictStrategy;
import structure.logic.monitor.GsonTools.JsonObjectExtensionConflictException;

public class AggregationMonitoringStrategy implements IMonitoringStrategy {

	private final Type stringObjectMap = new TypeToken<Map<String, Object>>() {}.getType();
	private final Gson gson = new Gson();
	private JsonObject cache = null;
	
	@Override
	public Map<String, Object> handleSensorJSON(String sensorData) {
		JsonObject sensorObject = gson.fromJson(sensorData, JsonElement.class).getAsJsonObject();
		if(cache != null) {
			try {
				GsonTools.extendJsonObject(sensorObject, ConflictStrategy.PREFER_FIRST_OBJ, cache);
			} catch (JsonObjectExtensionConflictException e) {
				e.printStackTrace();
			}
			cache = sensorObject;
		}
		cache = sensorObject;
		
		
		Map<String, Object> sensorMap = gson.fromJson(sensorObject, stringObjectMap);
		
		return sensorMap;
	}

}
