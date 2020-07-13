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

package util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

import org.cardygan.cmInstance.ComponentModelInstance;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLPackage;
import org.junit.Before;
import org.junit.Test;

import util.clafer.api.IClaferInstanceToUML;
import util.clafer.impl.ClaferInstanceToUML;

public class TestClaferInstanceToUMLImpl {

	private Model cm;
	
	@Before
	public void setUp() {
		
		ResourceSet resourceSet = createResourceSet();
		
		@SuppressWarnings("unused")
		UMLPackage umlPackage = UMLPackage.eINSTANCE;

		// Component model
		XMIResource resourceCM = null;
		String pathCm = "classpath:/SWIM-Rainbow/cm/cm.uml";
		String classpathRelativePath = pathCm.replaceFirst(Pattern.quote("classpath:"), "");
		final URL url = TestClaferInstanceToUMLImpl.class.getResource(classpathRelativePath);

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
	public void test1() {
		
		final String claferInstances = "\n" + 
				"HighRT\n" + 
				"MoreThanOneActiveServer\n" + 
				"DimmerDecreasable\n" + 
				"Adaptation\n" + 
				"  DecreaseDimmer\n" + 
				"    utilityResponseTime$3 -> -50\n" + 
				"    utilityServerCost$3 -> 10\n" + 
				"    utilityUserAnnoyance$3 -> -5\n" + 
				"  tacticResponseTime -> -50\n" + 
				"  tacticServerCost -> 10\n" + 
				"  tacticUserAnnoyance -> -5\n" + 
				"Constants\n" + 
				"  dimmerStep -> 10\n" + 
				"  numberOfDimmerLevels -> 5\n" + 
				"  rtThreshold -> 75\n" + 
				"  period -> 60\n" + 
				"Ctx\n" + 
				"  dimmer -> 50\n" + 
				"  servers -> 3\n" + 
				"  activeServers -> 3\n" + 
				"  responseTime -> 80\n" + 
				"  optResponseTime -> 8\n" + 
				"  maxServers -> 3\n" + 
				"  totalUtilization -> 90";
		
		IClaferInstanceToUML claferInstanceToUML = new ClaferInstanceToUML();
		ComponentModelInstance result = claferInstanceToUML.createCMInstance(claferInstances, cm);
		assertEquals(1, result.getComponentInstance().size());
		assertEquals("DecreaseDimmer", result.getComponentInstance().get(0).getTypeOfComponent().getName());
	}
	
	@Test
	public void test2() {
		final String claferInstances = "MoreThanOneActiveServer\n" + 
				"DimmerDecreasable\n" + 
				"Adaptation\n" + 
				"  tacticResponseTime -> 0\n" + 
				"  tacticServerCost -> 0\n" + 
				"  tacticUserAnnoyance -> 0\n" + 
				"Constants\n" + 
				"  dimmerStep -> 10\n" + 
				"  numberOfDimmerLevels -> 5\n" + 
				"  rtThreshold -> 75\n" + 
				"  period -> 60\n" + 
				"Ctx\n" + 
				"  dimmer -> 90\n" + 
				"  servers -> 3\n" + 
				"  activeServers -> 3\n" + 
				"  responseTime -> 8\n" + 
				"  optResponseTime -> 9\n" + 
				"  maxServers -> 3\n" + 
				"  totalUtilization -> 76";
		
		IClaferInstanceToUML claferInstanceToUML = new ClaferInstanceToUML();
		ComponentModelInstance result = claferInstanceToUML.createCMInstance(claferInstances, cm);
		assertEquals(0, result.getComponentInstance().size());
	}
	
}
