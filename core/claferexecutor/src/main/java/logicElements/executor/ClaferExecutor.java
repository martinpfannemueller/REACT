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

package logicElements.executor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cardygan.cmInstance.ComponentModelInstance;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLPackage;

import Manta.Effecting.Component;
import Manta.Effecting.ComponentChange;
import Manta.Effecting.Parameter;
import Manta.Knowledge.CMKnowledge;
import Manta.Knowledge.IKnowledgePrx;
import Manta.MAPECommunication.EffectingKnowledgeRecord;
import Manta.MAPECommunication.KnowledgeRecord;
import Manta.MAPECommunication.StringKnowledgeRecord;
import structure.logic.AbstractLogic;
import structure.logic.ILogic;
import structure.logic.InformationCategory;
import structure.logic.InformationType;
import structure.logic.LogicType;
import util.XMLEMFConverter;
import util.clafer.api.IClaferInstanceToUML;
import util.clafer.impl.ClaferInstanceToUML;
import util.cm.CmUtils;

public class ClaferExecutor extends AbstractLogic implements ILogic {
	
	private final Model componentModel;
	private final IClaferInstanceToUML claferInstanceToUML = new ClaferInstanceToUML();
	
	public ClaferExecutor(IKnowledgePrx knowledgeProxy) {
		super();
		this.informationCategory = InformationCategory.EXECUTOR;
		this.supportedInformationTypes.add(InformationType.Planning());
	
		this.informationType = InformationType.Executing();
		this.type = LogicType.EXECUTOR;
		this.shortName = "ClaferExecutor";
		
		CMKnowledge cmKnowledge = knowledgeProxy.getCMKnowledge();
		XMLEMFConverter xmlConverter = new XMLEMFConverter();
		
		try {
			componentModel = (Model)xmlConverter.loadEObjectFromString(cmKnowledge.value, UMLPackage.eINSTANCE);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("CMKnowledge not parseable");
		}
	}
	
	@Override
	public void initializeLogic(HashMap<String, String> properties) {

	}
	
	@Override
	public boolean callLogic(KnowledgeRecord record) {
		if (record instanceof StringKnowledgeRecord) {
			String claferInstances = ((StringKnowledgeRecord) record).data;
			
			ComponentModelInstance result = claferInstanceToUML.createCMInstance(claferInstances, componentModel);
			ComponentChange c = CmUtils.cmInstanceToComponentChange(result);
			
			List<Parameter> parameterChanges = new ArrayList<>();
			for(Component component : c.components) {
				for(Parameter p : component.parameters) {
					parameterChanges.add(p);
				}
			}
		
			EffectingKnowledgeRecord knowledgeRecord = new EffectingKnowledgeRecord(
					this.getInformationTypeAsString(),
					this.getInformationCategoryAsString(), 
					this.getShortName(), 
					System.currentTimeMillis() / 1000L,
					c,
					parameterChanges.toArray(new Parameter[parameterChanges.size()])
			);

			this.sendData(knowledgeRecord);
			return true;
			
		} else {
			throw new IllegalStateException("Unexpected KnowledgeRecord class");
		}
	}
	

}
