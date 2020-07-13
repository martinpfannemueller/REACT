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

import org.clafer.ast.*;
import org.clafer.javascript.*;
import org.eclipse.xtext.util.Files;

import util.clafer.api.ICfmToClafer;

import java.io.File;
import java.io.IOException;

public class CfmToClafer implements ICfmToClafer {

	private final static String CLAFER_EXEC_PATH_ENV = "CLAFER_EXEC_PATH";

	/**
	 * Convert Clafer File to Clafer Model
	 * 
	 * 
	 * @param fileName: String including path and filename of .cfr-file containing
	 *        claferized FM
	 * @return compiledClaferModel
	 */
	@Override
	public AstModel returnModel(String fileName) {

		// If a .cfr file is given, compile it first and change the input file
		// to the resulting .js file
		if (fileName.endsWith(".cfr")) {
			final String claferExec;
			if (System.getenv(CLAFER_EXEC_PATH_ENV) == null) {
				claferExec = "clafer";
			} else {
				claferExec = System.getenv(CLAFER_EXEC_PATH_ENV);
			}

			// compile the file
			try {
				ProcessBuilder builder = new ProcessBuilder(claferExec, "-k", "-m", "choco", fileName);
				builder.redirectErrorStream(true);
				Process compilerProcess = builder.start();
				compilerProcess.waitFor();
				if (compilerProcess.exitValue() != 0) {
					System.err.println(Files.readFileIntoString(fileName));
					System.out.println(Files.readFileIntoString(fileName));
					throw new IllegalStateException("Clafer compilation error: make sure your model is correct. Aborting...");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println(Files.readFileIntoString(fileName));
				System.out.println(Files.readFileIntoString(fileName));
				throw new IllegalStateException("Abnormal Clafer compiler termination. Aborting...");
			}

			File inputFile = new File(this.getBaseName(fileName) + ".js");
			inputFile.deleteOnExit();
			JavascriptFile javascriptFile = null;

			try {
				javascriptFile = Javascript.readModel(inputFile);

			} catch (IOException e) {

				throw new IllegalStateException("Error: Couldn't read from js-file. Aborting ...");
			}

			AstModel claferModel = javascriptFile.getModel();
			return claferModel;
		}
		return null;
	}

	private String getBaseName(String fileName) {
		int index = fileName.lastIndexOf('.');
		if (index == -1) {
			return fileName;
		} else {
			return fileName.substring(0, index);
		}
	}
}