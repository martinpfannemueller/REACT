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

package util.cm;

import java.util.ArrayList;
import java.util.List;

import org.cardygan.cmInstance.CmInstanceFactory;
import org.cardygan.cmInstance.ComponentInstance;
import org.cardygan.cmInstance.ComponentModelInstance;
import org.cardygan.cmInstance.PropertyInstance;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Property;

import Manta.Effecting.Component;
import Manta.Effecting.ComponentChange;
import Manta.Effecting.Parameter;

public class CmUtils {

	/**
	 * Searches for a UML class by <code>name</code>
	 * 
	 * @param componentModel
	 *            component model
	 * @param name
	 * 
	 * @return UML class matched by name or null if class with <code>name</code>
	 *         could be found
	 */
	public static org.eclipse.uml2.uml.Class getCMClassForName(Model componentModel, String name) {
		EList<EObject> modelContents = componentModel.eContents();

		for (EObject modelObject : modelContents) {

			if (modelObject.eClass().getName().equals("Class")) {
				if (((NamedElement) modelObject).getName().equals(name)) {
					return (org.eclipse.uml2.uml.Class) modelObject;
				}
			}
		}

		return null;
	}
	
	public static String cmInstanceToString(ComponentModelInstance cmInstance) {
		StringBuilder s = new StringBuilder();
		s.append("{");
		
		s.append("\"components\":");
		s.append("[");
		for (ComponentInstance instance : cmInstance.getComponentInstance()) {
			if (!instance.isIsEnvironmentComponent()) {
				s.append("{");
				s.append("\"name\":");
				s.append("\"");
				s.append(instance.getTypeOfComponent().getName());
				s.append("\"");
				s.append(",");
				s.append("\"properties\":");
				s.append("{");
				for (PropertyInstance propertyInstance : instance.getProperty()) {
					String propertyName = propertyInstance.getTypeOfProperty().getName();
					Object propertyValue = propertyInstance.getValueOfProperty();
					s.append("\"");
					s.append(propertyName);
					s.append("\"");
					s.append(":");
					s.append(propertyValue);
					if(instance.getProperty().indexOf(propertyInstance) != instance.getProperty().size()-1) {
						s.append(",");
					}
				}
				s.append("}");
				s.append("}");
			}
			if(cmInstance.getComponentInstance().indexOf(instance) != cmInstance.getComponentInstance().size()-1) {
				s.append(",");
			}
		}
		s.append("]");
		s.append("}");
		return s.toString();
	}
	
	public static List<Component> cmInstanceToComponentList(ComponentModelInstance cmInstance){
		List<Component> components = new ArrayList<>();
		for (ComponentInstance instance : cmInstance.getComponentInstance()) {
			
			Component c = new Component();
			c.className = instance.getTypeOfComponent().getName();
			
			List<Parameter> parameters = new ArrayList<>();
			for (PropertyInstance propertyInstance : instance.getProperty()) {
				Parameter p = new Parameter();
				p.key = propertyInstance.getTypeOfProperty().getName();
				p.value = String.valueOf(propertyInstance.getValueOfProperty());
				parameters.add(p);
			}
			
			c.parameters = parameters.toArray(new Parameter[parameters.size()]);
			
			components.add(c);
		}
		
		return components;
	}
	
	public static ComponentChange cmInstanceToComponentChange(ComponentModelInstance cmInstance) {
		List<Component> components = cmInstanceToComponentList(cmInstance);
		
		ComponentChange change = new ComponentChange();
		change.components = components.toArray(new Component[components.size()]);
		
		return change;
	}

	public static ComponentModelInstance ComponentChangeToCmInstance(final ComponentChange change, final Model componentModel, final String nameEnvironmentComponent) {
		
		final CmInstanceFactory instanceFactory = CmInstanceFactory.eINSTANCE;
		final ComponentModelInstance cmInstance = instanceFactory.createComponentModelInstance();
		
		for(Component c : change.components) {
			final org.eclipse.uml2.uml.Class componentClass = getCMClassForName(componentModel, c.className);
			
			if(componentClass != null) {
				// Create Instance of the environment component
				ComponentInstance compInstance = instanceFactory.createComponentInstance();
				compInstance.setTypeOfComponent(componentClass);
				
				if(c.className.equals(nameEnvironmentComponent)) {
					compInstance.setIsEnvironmentComponent(true);
				}else {
					compInstance.setIsEnvironmentComponent(false);
				}

				// Add all properties to the instance of the environment component
				for (Property prop : componentClass.allAttributes()) {
					
					Parameter parameter = null;
					for(Parameter p : c.parameters) {
						if(p.key.equals(prop.getName())) {
							parameter = p;
							break;
						}
					}
					
					if(parameter != null) {
						PropertyInstance propInstance = instanceFactory.createPropertyInstance();
						propInstance.setTypeOfProperty(componentClass.getAttribute(prop.getName(), prop.getType()));
						propInstance.setValueOfProperty(parameter.value);
						compInstance.getProperty().add(propInstance);
					}else {
						System.out.println("Parameter " + prop.getName() + " could not be found in components");
					}
				}

				// Add the instance of environment component to the instance of the component
				// model
				cmInstance.getComponentInstance().add(compInstance);
			}else {
				System.out.println("Class " + c.className + " could not be found in component model");
			}
		}
		
		return cmInstance;
	}
	
}
