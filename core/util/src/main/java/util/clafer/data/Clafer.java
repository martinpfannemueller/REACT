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

package util.clafer.data;

import java.util.ArrayList;
import java.util.List;

public class Clafer {

	private String name;
	private boolean isAbstract = false;
	private String claferType;
	private List<Attribute> attributes = new ArrayList<>();
	private List<Clafer> clafers = new ArrayList<>();
	
	public Clafer() {
		
	}
	
	public Clafer(final String name, final boolean isAbstract) {
		this.name = name;
		this.isAbstract = isAbstract;
	}
	
	public String getName() {
		return name;
	}
	public boolean isAbstract() {
		return isAbstract;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}
	public String getClaferType() {
		return claferType;
	}
	public void setClaferType(String claferType) {
		this.claferType = claferType;
	}

	public void addAttribute(Attribute attribute) {
		this.attributes.add(attribute);
	}
	
	public List<Attribute> getAttributes() {
		return this.attributes;
	}
	public void addClafer(Clafer clafer) {
		this.clafers.add(clafer);
	}
	
	public List<Clafer> getClafers() {
		return this.clafers;
	}
	@Override
	public String toString() {
		return "ClaferDTO [name=" + name + ", claferType=" + claferType + ", attributes=" + attributes + ", clafers=" + clafers + "]";
	}
	
}
