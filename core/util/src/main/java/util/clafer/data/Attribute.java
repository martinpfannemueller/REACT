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

public class Attribute {

	private String key;
	private String type;
	private int lb = 1;
	private int ub = 1;
	private Object value;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getLb() {
		return lb;
	}
	public void setLb(int lb) {
		this.lb = lb;
	}
	public int getUb() {
		return ub;
	}
	public void setUb(int ub) {
		this.ub = ub;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
	public String getCleanKey() {
		if(this.getKey().contains("$")) {
			String key = this.getKey();
			String[] parts = key.split("\\$");
			return parts[0];
		}
		return this.getKey();
	}
	
	@Override
	public String toString() {
		return "AttributeDTO [key=" + key + ", value=" + value + "]";
	}
	
}
