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

package structure.logic;

import java.util.ArrayList;
import java.util.HashMap;

import Manta.MAPECommunication.KnowledgeRecord;

public abstract class AbstractLogic implements ILogic {

	protected ILogicContainer container;
	protected InformationCategory informationCategory;
	protected LogicType type;
	protected String shortName;

	
	protected InformationType informationType;
	protected ArrayList<InformationType> supportedInformationTypes = new ArrayList<InformationType>();
	
	
	public AbstractLogic(ILogicContainer container, InformationCategory informationCategory, InformationType informationType) {
		this.container = container;
		this.informationCategory = informationCategory;
		this.informationType = informationType;
	}
	
	public AbstractLogic() {
		
	}

	@Override
	public boolean isCompatibleDataType(String dataType) {
		for (InformationType type : supportedInformationTypes) {
			if (type.toString().equals(dataType)) return true;
		}
		return false;
	}	
	
	@Override
	public String getInformationCategoryAsString() {
		return informationCategory.toString();
	}


	@Override
	public String getInformationTypeAsString() {
		return informationType.toString();
	}
	
	@Override
	public abstract boolean callLogic(KnowledgeRecord data);

	@Override
	public LogicType getLogicType() {
		return type;
	}

	@Override
	public String getShortName() {
		return shortName;
	}
	
	protected void sendData(KnowledgeRecord data) {
		container.prepareDataForSending(data, informationType);
	}

	@Override
	public InformationCategory getInformationCategory() {
		return informationCategory;
	}

	@Override
	public void setInformationCategory(InformationCategory informationCategory) {
		this.informationCategory = informationCategory;
	}

	@Override
	public InformationType getInformationType() {
		return informationType;
	}

	@Override
	public void setContainer(ILogicContainer container) {
		this.container = container;
	}
	
	public String toString() {
		StringBuilder info = new StringBuilder();
		info.append("Logic Information" + "\n");
		info.append("informationCategory: " + informationCategory + "\n");
		info.append("type: " + type + "\n");
		info.append("shortName: " + shortName + "\n");
		info.append("supportedInformationTypes: " + "\n");
		for (InformationType supType: supportedInformationTypes) {
			info.append(supType + "\n");
		}
		return info.toString();
	}
	
	@Override
	public void initializeLogic(HashMap<String, String> properties) {
		// intended to be empty
	}
} 

