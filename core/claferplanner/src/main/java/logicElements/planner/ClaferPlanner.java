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

package logicElements.planner;

import java.util.HashMap;

import Manta.Knowledge.IKnowledgePrx;
import Manta.MAPECommunication.KnowledgeRecord;
import Manta.MAPECommunication.StringKnowledgeRecord;
import structure.logic.AbstractLogic;
import structure.logic.ILogic;
import structure.logic.InformationCategory;
import structure.logic.InformationType;
import structure.logic.LogicType;
import util.clafer.api.IConfigurator;
import util.clafer.api.IConfigurator.BackendType;
import util.clafer.impl.Configurator;

public class ClaferPlanner extends AbstractLogic implements ILogic {

	private final IKnowledgePrx knowledgeProxy;
	private final String AOS;
	private final IConfigurator configurator = new Configurator();
	
	public ClaferPlanner(IKnowledgePrx knowledgeProxy) {
		super();
		this.informationCategory = InformationCategory.PLANNER;
		this.supportedInformationTypes.add(InformationType.Analyzing());
	
		this.informationType = InformationType.Planning();
		this.type = LogicType.PLANNER;
		this.shortName = "ClaferPlanner";
		
		this.knowledgeProxy = knowledgeProxy;
		this.AOS = this.knowledgeProxy.getClaferKnowledge().value;
	}
	
	@Override
	public void initializeLogic(HashMap<String, String> properties) {

	}
	
	@Override
	public boolean callLogic(KnowledgeRecord record) {
		if (record instanceof StringKnowledgeRecord) {
			String partialClaferInstances = ((StringKnowledgeRecord) record).data;
			
			try {
				String instanceString = configurator.generateInstances(
						this.AOS + 	"\n\n" + partialClaferInstances, 
						1, 
						BackendType.Choco
				);
				
				StringKnowledgeRecord knowledgeRecord = new StringKnowledgeRecord(
						this.getInformationTypeAsString(), 
						this.getInformationCategoryAsString(), 
						this.getShortName(), 
						System.currentTimeMillis() / 1000L, 
						instanceString
				);
				
				this.sendData(knowledgeRecord);
				return true;
			}catch(Exception e) {					
				e.printStackTrace();
				return false;
			}
		} else {
			throw new IllegalStateException("Unexpected KnowledgeRecord class");
		}
	}
}
