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

import java.util.ArrayList;
import java.util.List;

import org.cardygan.cmInstance.CmInstanceFactory;
import org.cardygan.cmInstance.ComponentInstance;
import org.cardygan.cmInstance.ComponentModelInstance;
import org.cardygan.cmInstance.PropertyInstance;
import org.cardygan.cmInstance.util.CmInstanceUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Property;

import util.clafer.api.IClaferInstanceToUML;
import util.clafer.data.Attribute;
import util.clafer.data.Clafer;
import util.cm.CmUtils;
import util.emf.EMFUtils;

public class ClaferInstanceToUML implements IClaferInstanceToUML {

	private final CmInstanceFactory factory = CmInstanceFactory.eINSTANCE;
	
	@Override
	public ComponentModelInstance createCMInstance(String instanceString, Model model) {

		List<Clafer> topClafers = new ArrayList<>();
		List<Clafer> clafers = new ArrayList<>();

		if (instanceString.startsWith("\n")) {
			instanceString = instanceString.replaceFirst("\n", "");
		}

		final String[] lines = instanceString.split("\n");
		for (String line : lines) {
			if (!line.startsWith(" ")) { // New Clafer
				Clafer clafer = this.parseClafer(line);
				topClafers.add(clafer);
				clafers.add(clafer);
			} else if (line.startsWith(" ")) { // Attribute
				line = line.replaceAll("[\\s|\\u00A0]+", "");
				
				// Check if attribute value or sub clafer
				if (line.contains("->")) { // attribute value
					final String[] parts = line.split("->");
					String attribute = parts[0];
					String value = parts[1];

					if (attribute.startsWith("a_")) {
						attribute = parts[0].replaceFirst("a_", "");
					}
					
					if(attribute.contains("$")) {
						final String[] anonymousParts = attribute.split("\\$");
						attribute = anonymousParts[0];
					}
					
					if(value.startsWith("\"") && value.endsWith("\"")) {
						value = value.replaceAll("\"", "");
					}
					
					for(String innerLine : lines) {
						if(!innerLine.startsWith(" ")) {
							if(innerLine.startsWith(value)) { // This is a string replacement Clafer
								value = value.replaceFirst("[^\\W_]+_", "");
								
								value = value.replaceAll("__", ":");
								value = value.replaceAll("_", ".");
								
								// Handle operators
								value = value.replaceAll(".lte.", ".<=.");
								value = value.replaceAll(".hte.", ".>=.");
								value = value.replaceAll(".lt.", ".<.");
								value = value.replaceAll(".ht.", ".>.");
								value = value.replaceAll(".eq.", ".=.");
								value = value.replaceAll(".ne.", ".!=.");
								
								value = value.replaceAll(".OR.", ".||.");
								value = value.replaceAll(".AND.", ".&&.");
							}
						}
					}

					Attribute attributeObject = new Attribute();
					attributeObject.setKey(attribute);
					attributeObject.setValue(value);
					clafers.get(clafers.size() - 1).addAttribute(attributeObject);
				}else { // Sub Clafer
					Clafer clafer = this.parseClafer(line);
					clafers.get(clafers.size() - 1).addClafer(clafer);
					clafers.add(clafer);
				}
				
			}
		}
		
		ComponentModelInstance cmInstance = factory.createComponentModelInstance();
		
		for(Clafer clafer : clafers) {
			
			org.eclipse.uml2.uml.Class componentClass = CmUtils.getCMClassForName(model, clafer.getClaferType());
			
			if(componentClass != null) {
				
				ComponentInstance componentInstance = CmInstanceUtils.getComponentInstanceByName(cmInstance, clafer.getName());
				
				if(componentInstance == null) {
					componentInstance = this.setupComponentInstance(clafer, componentClass, cmInstance);
				}
				
				for(Attribute attribute : clafer.getAttributes()) {
					this.assignAttribute(attribute, componentInstance, componentClass);
				}
				
			}
			
		}
		
		return cmInstance;
	}
	
	private Clafer parseClafer(String line) {
		line = line.replaceAll("[\\s|\\u00A0]+", "");
		
		final String[] parts = line.split(":");

		String name = parts[0];

		String claferType = null;
		if (parts.length > 1) {
			claferType = parts[1];
		}

		if (name.startsWith("f_")) {
			name = parts[0].replaceFirst("f_", "");
		}

		// Check for anonymous Clafers
		if(parts.length == 1 || name.contains("$")) {
			final String[] anonymousParts = name.split("\\$");
			name = "";
			claferType = anonymousParts[0];
		}
		
		Clafer clafer = new Clafer();
		clafer.setName(name);
		clafer.setClaferType(claferType);
		
		return clafer;
	}

	private ComponentInstance setupComponentInstance(Clafer clafer, org.eclipse.uml2.uml.Class instanceClass, ComponentModelInstance cmInstance) {

		if (instanceClass == null || cmInstance == null) {
			throw new IllegalArgumentException("setupComponentInstance: No parameters being null allowed.");
		}

		ComponentInstance compInstance = factory.createComponentInstance();
		compInstance.setTypeOfComponent(instanceClass);
		
		cmInstance.getComponentInstance().add(compInstance);

		return compInstance;
	}
	
	private void assignAttribute(Attribute attribute, ComponentInstance componentInstance, org.eclipse.uml2.uml.Class componentClass) {
		EList<Property> properties = componentClass.getAttributes();

		// Create a new property instance for component instance
		PropertyInstance propInstance = factory.createPropertyInstance();
		Property property = EMFUtils.getPropertyForName(properties, attribute.getCleanKey());
		
		if(property == null) {
			System.out.println("Skipped attribute " + attribute.getCleanKey());
		}else {
			propInstance.setTypeOfProperty(componentClass.getAttribute(attribute.getCleanKey(), property.getType()));

			propInstance.setValueOfProperty(attribute.getValue());

			// Add actual property instance to component instance
			componentInstance.getProperty().add(propInstance);
		}
	}
	
}
