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

package logicElements.analyzer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import Manta.Knowledge.IKnowledgePrx;
import Manta.MAPECommunication.KnowledgeRecord;
import Manta.MAPECommunication.StringKnowledgeRecord;
import structure.logic.AbstractLogic;
import structure.logic.ILogic;
import structure.logic.InformationCategory;
import structure.logic.InformationType;
import structure.logic.LogicType;
import util.clafer.api.IJsonToClaferInstance;
import util.clafer.data.Clafer;
import util.clafer.impl.AOSParser;
import util.clafer.impl.JsonToClaferInstance;

public class ClaferAnalyzer extends AbstractLogic implements ILogic {

	private final IKnowledgePrx knowledgeProxy;
	private final String AOS;
	
	private final IJsonToClaferInstance jsonToClaferInstance = new JsonToClaferInstance();
	
	private final Type stringObjectMap = new TypeToken<Map<String, Object>>() {
	}.getType();
	private final Gson gson = new Gson();

	public ClaferAnalyzer(IKnowledgePrx knowledgeProxy) {
		super();
		this.informationCategory = InformationCategory.ANALYZER;
		this.supportedInformationTypes.add(InformationType.Monitoring());

		this.informationType = InformationType.Analyzing();
		this.type = LogicType.ANALYZER;
		this.shortName = "ClaferAnalyzer";
		
		this.knowledgeProxy = knowledgeProxy;
		this.AOS = this.knowledgeProxy.getClaferKnowledge().value;
	}

	@Override
	public void initializeLogic(HashMap<String, String> properties) {

	}

	@Override
	public boolean callLogic(KnowledgeRecord record) {
		if (record instanceof StringKnowledgeRecord) {
			String recordData = ((StringKnowledgeRecord) record).data;

			final Map<String, Object> monitoringData = gson.fromJson(recordData, stringObjectMap);

			final List<Clafer> abstractClafers = AOSParser.getAbstractClafers(this.AOS);
			final String claferInstances = this.jsonToClaferInstance.createClaferInstance(monitoringData, abstractClafers);

			StringKnowledgeRecord knowledgeRecord = new StringKnowledgeRecord(
					this.getInformationTypeAsString(),
					this.getInformationCategoryAsString(), 
					this.getShortName(), 
					System.currentTimeMillis() / 1000L,
					claferInstances
			);

			this.sendData(knowledgeRecord);
			return true;
		} else {
			throw new IllegalStateException("Unexpected KnowledgeRecord class");
		}
	}
}
