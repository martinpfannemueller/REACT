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

package logicElements.monitor;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import Manta.MAPECommunication.KnowledgeRecord;
import Manta.MAPECommunication.StringKnowledgeRecord;
import structure.logic.AbstractLogic;
import structure.logic.ILogic;
import structure.logic.InformationCategory;
import structure.logic.InformationType;
import structure.logic.LogicType;
import structure.logic.monitor.AggregationMonitoringStrategy;
import structure.logic.monitor.DefaultMonitoringStrategy;
import structure.logic.monitor.IMonitoringStrategy;

public class ClaferMonitor extends AbstractLogic implements ILogic {

	private final Gson gson = new Gson();
	private IMonitoringStrategy monitoringStrategy;
	private final String KEY_MONITORING_STRATEGY = "monitoringStrategy";
	
	public ClaferMonitor() {
		this.informationCategory = InformationCategory.MONITOR;
		this.supportedInformationTypes.add(InformationType.Sensor());
	
		this.informationType = InformationType.Monitoring();
		this.type = LogicType.MONITOR;
		this.shortName = "ClaferMonitor";
	}
	
	@Override
	public void initializeLogic(HashMap<String, String> properties) {
		if(properties.containsKey(KEY_MONITORING_STRATEGY) && properties.get(KEY_MONITORING_STRATEGY) != null) {
			if(properties.get(KEY_MONITORING_STRATEGY).equals("aggregation")) {
				monitoringStrategy = new AggregationMonitoringStrategy();
			}else if(properties.get(KEY_MONITORING_STRATEGY).equals("windowing")) {
				monitoringStrategy = new AggregationMonitoringStrategy();
			}else {
				monitoringStrategy = new DefaultMonitoringStrategy();
			}
		}else {
			monitoringStrategy = new DefaultMonitoringStrategy();
		}
	}
	
	@Override
	public boolean callLogic(KnowledgeRecord record) {
		if (record instanceof StringKnowledgeRecord) {
			String recordData = ((StringKnowledgeRecord)record).data;
			
			Map<String, Object> sensorMap = monitoringStrategy.handleSensorJSON(recordData);
			
			StringKnowledgeRecord knowledgeRecord = new StringKnowledgeRecord(
					this.getInformationTypeAsString(), 
					this.getInformationCategoryAsString(), 
					this.getShortName(), 
					System.currentTimeMillis() / 1000L, 
					gson.toJson(sensorMap)
			);
			
			this.sendData(knowledgeRecord);
			return true;
		}else {
			throw new IllegalStateException("Unexpected KnowledgeRecord class");
		}
	}

}
