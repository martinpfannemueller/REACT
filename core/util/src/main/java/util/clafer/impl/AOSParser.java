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

import util.clafer.data.Attribute;
import util.clafer.data.Clafer;

/**
 * 
 * @author Martin Pfannemüller
 * 
 *         Minimal class for parsing the AOS. The result is used to check for
 *         abstract Clafers and check if the monitoring data can be mapped.
 *
 */
public class AOSParser {

	public static List<Clafer> getAbstractClafers(final String AOS){
		final List<Clafer> abstractClafers = new ArrayList<>();
		
		boolean inAbstractClafer = false;
		
		for(String line : AOS.split("\n")) {
			if(line.contains("abstract")) {
				final String claferName = line.split(" ")[1]; // Name
				
				Clafer clafer = new Clafer(claferName, true);
				abstractClafers.add(clafer);
				inAbstractClafer = true;
			}else if((line.contains("\t") || line.contains("    ")) && inAbstractClafer && !line.replaceAll("\\s", "").startsWith("[") ) {
				final String[] lineComponents = line.split("->");
				final String attributeName = lineComponents[0];
				final String attributeCardinality;
				final String attributeType;
				
				if(lineComponents.length > 1) {
					final String[] attributeComponents = lineComponents[1].split(" ");
					
					if(attributeComponents.length == 3) {
						attributeCardinality = attributeComponents[2];
						attributeType = attributeComponents[1];
					}else if(attributeComponents.length == 2){
						attributeCardinality = "";
						attributeType = attributeComponents[1];
					}else {
						throw new IllegalStateException("Unexpected attribute elements: " + attributeComponents);
					}
					
					Attribute attribute = new Attribute();
					attribute.setKey(attributeName);
					attribute.setType(attributeType);
					
					if(attributeCardinality != null && !attributeCardinality.isEmpty()) {
						final String[] cardinalities = attributeCardinality.split("\\.\\.");
						
						if(cardinalities.length == 2) {
							final int lb = Integer.parseInt(cardinalities[0]);
							final int ub = Integer.parseInt(cardinalities[1]);
							attribute.setLb(lb);
							attribute.setUb(ub);
						}
					}
					
					Clafer clafer = abstractClafers.get(abstractClafers.size()-1);
					clafer.addAttribute(attribute);
				}
			}else if(!line.contains("\t")) {
				inAbstractClafer = false;
			}
		}
		
		return abstractClafers;
	}
	
}
