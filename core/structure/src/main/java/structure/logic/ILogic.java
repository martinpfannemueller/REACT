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

import java.util.HashMap;

import Manta.MAPECommunication.KnowledgeRecord;

public interface ILogic {

	/** Call Logic Method performing the functionality. */
	public boolean callLogic(KnowledgeRecord data);
	
	/** Setter for the corresponding container reference. */
	public void setContainer(ILogicContainer container);
	
	/** Checks whether the logic is compatible with the specified data type. */
	public boolean isCompatibleDataType(String dataType);
	
	/** Getter for the logic type. */
	public LogicType getLogicType();
	
	/** Getter for the logic short name. */
	public String getShortName();
	
	/** Getter for the information category. */
	public InformationCategory getInformationCategory();
	
	/** Getter for the information category. */
	public String getInformationCategoryAsString();

	/** Setter for the information category. */
	public void setInformationCategory(InformationCategory informationCategory);

	/** Getter for the information type. */
	public InformationType getInformationType();
	
	/** Getter for the information type as String. */
	public String getInformationTypeAsString();
	
	public void initializeLogic(HashMap<String, String> properties);
}
