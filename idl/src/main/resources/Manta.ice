module Manta{

	module Effecting{
	
		struct Parameter{
			string key;
			string value;
		}
	
		sequence<Parameter> parameters;
		
		struct Component{
			string className;
			parameters parameters;
		}
		
		sequence<Component> components;
	
		struct ParameterChange{
			Parameter parameter;
		}
		
		struct ComponentChange{
			components components;
		}
		
		interface ManagedResource{
			void sendParameterChanges(ParameterChange parameters);
			void sendComponentChanges(ComponentChange components);
		}
	
	}
	
	module MAPECommunication{
	
		sequence<Effecting::Parameter> ParameterChanges;
	
		class KnowledgeRecord{
			string type;
			string category;
			string ownerID;
			long timeStamp;
		}
		
		class StringKnowledgeRecord extends KnowledgeRecord{
			string data;
		}
		
		class EffectingKnowledgeRecord extends KnowledgeRecord{
			Effecting::ComponentChange components;
			ParameterChanges parameterChanges;
		}
		
		class ComponentKnowledgeRecord extends KnowledgeRecord{
			Effecting::ComponentChange components;
		}
	
		interface IALElement{
			void callLogic(KnowledgeRecord knowledgeRecord);
			void setSuccessor(string successorString);
		}
		
	}

	module Knowledge{
	
		class KnowledgePart{
			string value;
		}
		
		class CMKnowledge extends KnowledgePart{}
		class ClaferKnowledge extends KnowledgePart{}
	
		interface IKnowledge{
			void sendKnowledge(KnowledgePart knowledge);
			CMKnowledge getCMKnowledge();
			ClaferKnowledge getClaferKnowledge();
		}
		
	}
	
	module Sensing{

		interface ISensor{
			void receiveSensorData(string sensorData);
		}
		
	}

}