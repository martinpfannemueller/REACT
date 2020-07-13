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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.uml2.uml.UMLPackage;

public class XMLEMFConverter extends AConverter implements IConverter {

	private ResourceSet resourceSet;
	private HashMap<Class<?>, String> packageClassFileExtension = new HashMap<>();

	public XMLEMFConverter() {
		ResourceSet resourceSet = new ResourceSetImpl();
	    resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
	    EPackage umlPackage = UMLPackage.eINSTANCE;
	    resourceSet.getPackageRegistry().put(umlPackage.getNsURI(), umlPackage);
	    this.resourceSet = resourceSet;
	    
	    packageClassFileExtension = new HashMap<>();
        packageClassFileExtension.put(UMLPackage.eINSTANCE.getClass(), "uml");
	}
	
	public void addClassMapping(final Class<?> mappingClass, final String extension) {
		packageClassFileExtension.put(mappingClass, extension);
	}
	
	@Override
	public EObject loadEObjectFromString(String string, EPackage ePackage) throws IOException {
		
		if(string == null || string.equals("")){
			throw new IllegalArgumentException("Empty string");
		}
		
		if(ePackage == null){
			throw new IllegalArgumentException("ePackage is null");
		}
		
		String extension = packageClassFileExtension.get(ePackage.getClass());
		
		if(extension == null) {
			throw new IllegalStateException("Missing file extension for conversion");
		}
		
	    Resource resource = this.resourceSet.createResource(URI.createURI("*." + extension));
	    resource.load(new URIConverter.ReadableInputStream(string), null);
	    return resource.getContents().get(0);
	}

	@Override
	public String getStringfromEObject(EObject object, EPackage ePackage) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		Resource res = this.resourceSet.createResource(URI.createURI("*." + object.getClass()));
		res.getContents().add(EcoreUtil.copy(object));
		try {
			res.save(stream, null);
		} catch (IOException ioe) {
		  ioe.printStackTrace();
		}
		return stream.toString();
	}
	
	private String objectToString(Object hashMap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLEncoder xmlEncoder = new XMLEncoder(bos);
        xmlEncoder.writeObject(hashMap);
        xmlEncoder.close();
        return bos.toString();
    }

	private Object stringToObject(String string) {
        XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(string.getBytes()));
        Object o = xmlDecoder.readObject();
        xmlDecoder.close();
        return o;
    }
	
}
