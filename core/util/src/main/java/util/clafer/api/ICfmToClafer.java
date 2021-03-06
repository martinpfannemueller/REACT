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

package util.clafer.api;

import org.clafer.ast.AstModel;

public interface ICfmToClafer {
	
	/**
	 *  Convert Clafer File to Clafer Model
	 *  
	 *  
	 * @param fileName: String including path and filename of .cfr-file
     * @return compilated ClaferModel
	 */
	public AstModel returnModel(String fileName);
}