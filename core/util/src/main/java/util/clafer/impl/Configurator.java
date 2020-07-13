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

import org.clafer.compiler.*;
import org.clafer.instance.InstanceModel;
import org.clafer.scope.*;

import util.clafer.api.ICfmToClafer;
import util.clafer.api.IConfigurator;

import org.clafer.ast.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Configurator implements IConfigurator {

	private final ICfmToClafer cfmtoclaferConverter = new CfmToClafer();

	@Override
	public String generateInstances(String claferString, int maxNoOfInstances, BackendType backendType) {
		
		String fileName = "";

		File tempFile = new File("Configurator.cfr");
		tempFile.deleteOnExit();
		fileName = tempFile.getAbsolutePath();

		try (FileWriter fw = new FileWriter(fileName)) {
			fw.write(claferString);
		} catch (IOException e) {
			throw new IllegalStateException("Configurator Error: Couldn't write to temporary cfr-File. Aborting...");
		}
		
		AstModel claferModel = cfmtoclaferConverter.returnModel(fileName);

		ClaferSolver solver = org.clafer.compiler.ClaferCompiler.compile(claferModel, Scope.defaultScope(10).intLow(-256).intHigh(256));

		int actNoOfInst = 0;
		while (solver.find() && actNoOfInst < maxNoOfInstances) {
			InstanceModel clfInstance = solver.instance();
			
			return clfInstance.toString();
		}
		
		return "";
	}

	
}