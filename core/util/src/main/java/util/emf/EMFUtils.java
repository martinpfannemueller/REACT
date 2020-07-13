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

package util.emf;

import java.io.File;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Property;

public class EMFUtils
{

   /**
    * Maps the platform:/resource/ to the given path within the given {@link ResourceSet}
    * @param resourceSet the resource set to configure
    * @param workspaceRootPath the root for platform-resource URIs
    */
   public static final void setWorkspaceRootDirectory(ResourceSet resourceSet, String workspaceRootPath) {
      resourceSet.getURIConverter().getURIMap().put(
            URI.createPlatformResourceURI("/", true), URI.createFileURI(workspaceRootPath + File.separatorChar));
   }
   
   /**
    * Searches in an EList of properties for a property with <code>name</code>
    * @param properties the EList with properties
    * @param name the name the property list is searched for
    * @return Property if a property with the <code>name</code> is found or null if not
    */
   public static Property getPropertyForName(EList<Property> properties, String name) {
		for (Property property : properties) {
			if (property.getName().equals(name)) {
				return property;
			}
		}
		return null;
	}

}

