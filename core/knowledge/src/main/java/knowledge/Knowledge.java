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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLPackage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zeroc.Ice.Current;

import Manta.Knowledge.CMKnowledge;
import Manta.Knowledge.ClaferKnowledge;
import Manta.Knowledge.IKnowledge;
import Manta.Knowledge.KnowledgePart;
import util.XMLEMFConverter;

public class Knowledge implements IKnowledge {
	
	private static Knowledge instance;

	private Model componentModel;
	private String claferString;
	
	private final Gson gson = new Gson();

	private XMLEMFConverter converter;

	public Knowledge() {
		converter = new XMLEMFConverter();
		converter.addClassMapping(UMLPackage.eINSTANCE.getClass(), "uml");
	}

	public Knowledge(XMLEMFConverter converter) {
		this.converter = converter;
	}

	public static synchronized Knowledge getInstance() {
		if (Knowledge.instance == null) {
			Knowledge.instance = new Knowledge();
		}
		return Knowledge.instance;
	}

	public static synchronized Knowledge getInstance(XMLEMFConverter converter) {
		if (Knowledge.instance == null) {
			Knowledge.instance = new Knowledge(converter);
		} else {
			if (instance.converter != converter) {
				instance.converter = converter;
			}
		}
		return Knowledge.instance;
	}

	@Override
	public void sendKnowledge(KnowledgePart knowledge, Current current) {

		try {
			if(knowledge.getClass().equals(CMKnowledge.class)) {
				this.componentModel = (Model) converter.loadEObjectFromString(knowledge.value, UMLPackage.eINSTANCE);
			}else if(knowledge.getClass().equals(ClaferKnowledge.class)) {
				this.claferString = knowledge.value;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public CMKnowledge getCMKnowledge(Current current) {
		CMKnowledge knowledge = new CMKnowledge();
		knowledge.value = converter.getStringfromEObject(this.componentModel, UMLPackage.eINSTANCE);
		return knowledge;
	}

	@Override
	public ClaferKnowledge getClaferKnowledge(Current current) {
		ClaferKnowledge knowledge = new ClaferKnowledge();
		knowledge.value = this.claferString;
		return knowledge;
	}

}
