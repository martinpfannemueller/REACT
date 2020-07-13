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

package knowledge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLPackage;
import org.junit.Before;
import org.junit.Test;

import Manta.Knowledge.CMKnowledge;
import util.XMLEMFConverter;

public class XMLCMTest {

	private Model cm;
	private XMLEMFConverter converter = new XMLEMFConverter();
	
	@Before
	public void setUp() {
		
		ResourceSet resourceSet = createResourceSet();
		
		@SuppressWarnings("unused")
		UMLPackage umlPackage = UMLPackage.eINSTANCE;

		// Component model
		XMIResource resourceCM = null;
		String pathCm = "classpath:/tc.cm.uml";
		String classpathRelativePath = pathCm.replaceFirst(Pattern.quote("classpath:"), "");
		final URL url = XMLCMTest.class.getResource(classpathRelativePath);

		resourceCM = (XMIResource) resourceSet.createResource(URI.createFileURI(url.getPath()));
		try {
			resourceCM.load(null);
			cm = (Model) resourceCM.getContents().get(0);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	private ResourceSet createResourceSet() {
		final ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION,
				new XMIResourceFactoryImpl());
		return rs;
	}
	
	@Test
	public void setGetCompare() {
		
		try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize())
        {
            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("KnowledgeAdapter", "default -p 10000");
            com.zeroc.Ice.Object object = Knowledge.getInstance(converter);
            adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("Knowledge"));
            adapter.activate();
            
            com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("Knowledge:default -p 10000");
            Manta.Knowledge.IKnowledgePrx knowledge = Manta.Knowledge.IKnowledgePrx.checkedCast(base);
            if(knowledge == null)
            {
                throw new Error("Invalid proxy");
            }

            CMKnowledge part = new CMKnowledge();
            part.value = converter.getStringfromEObject(cm, UMLPackage.eINSTANCE);
            knowledge.sendKnowledge(part);
            
            CMKnowledge cmPart = knowledge.getCMKnowledge();
            assertEquals(part.value, cmPart.value);
            
            Model loadedModel = null;
            try {
            	loadedModel = (Model)converter.loadEObjectFromString(cmPart.value, UMLPackage.eINSTANCE);
			} catch (IOException e) {
				e.printStackTrace();
			}
            
            compareUMLModels(this.cm, loadedModel);
        }
	}
	
	private void compareUMLModels(Model m1, Model m2) {
		assertEquals(m1.getName(), m2.getName());
		assertEquals(m1.eContents().size(), m2.eContents().size());
		
		for(int i = 0; i < m1.eContents().size(); i++) {
			EObject e1 = m1.eContents().get(i);
			EObject e2 = m2.eContents().get(i);
			
			assertTrue(e1.eClass().getName().equals("Class"));
			assertTrue(e2.eClass().getName().equals("Class"));
			
			org.eclipse.uml2.uml.Class class1 = (org.eclipse.uml2.uml.Class)e1;
			org.eclipse.uml2.uml.Class class2 = (org.eclipse.uml2.uml.Class)e1;
			assertEquals(((NamedElement) class1).getName(), ((NamedElement) class2).getName());

			assertEquals(class1.getAllAttributes().size(), class2.getAllAttributes().size());
        	
			for(int j = 0; j < class1.getAllAttributes().size(); j++) {
				Property p1 = class1.getAllAttributes().get(j);
				Property p2 = class2.getAllAttributes().get(j);
				
				assertEquals(p1.getName(), p2.getName());
				assertEquals(p1.getType(), p2.getType());
			}
		}	
	}

}
