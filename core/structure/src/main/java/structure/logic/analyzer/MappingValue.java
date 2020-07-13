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

package structure.logic.analyzer;

public class MappingValue implements Comparable<MappingValue> {

	private String rawValue;
	private String condition;
	private String value;
	
	public MappingValue(String rawValue) {
		this.rawValue = rawValue;
		
		if(rawValue.contains("<=") || rawValue.contains(">=") || rawValue.contains("<") || rawValue.contains(">")) { // rawValue is not simply a value but has a condition
			if(rawValue.contains("<=")) {
				this.condition = "<=";
				this.value = this.rawValue.replace("<=", "");
			}else if(rawValue.contains(">=")) {
				this.condition = ">=";
				this.value = this.rawValue.replace(">=", "");
			}else if(rawValue.contains("<")) {
				this.condition = "<";
				this.value = this.rawValue.replace("<", "");
			}else if(rawValue.contains(">")) {
				this.condition = ">";
				this.value = this.rawValue.replace(">", "");
			}
			
	    }else {
	    		this.value = rawValue;
	    }
	}
	
	public boolean hasCondition() {
		if(condition != null && !condition.equals("")) {
			return true;
		}
		return false;
	}
	
	private String getCondition() {
		return this.condition;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public String getRawValue() {
		return this.rawValue;
	}
	
	public double getDoubleValue() {
		return Double.valueOf(this.value);
	}
	
	public int getIntegerValue() {
		return Integer.valueOf(this.value);
	}
	
	public boolean matchValue(Object valueToCompare) {
		
		if(valueToCompare == null) {
			throw new IllegalStateException("valueToCompare cannot be null");
		}
		
		if(valueToCompare instanceof String) {
			String stringValueToCompare = (String)valueToCompare;
			return this.matchValue(stringValueToCompare);
		}else if(valueToCompare instanceof Double || valueToCompare instanceof Integer) {
			double doubleValueToCompare = (Double)valueToCompare;
			return this.matchValue(doubleValueToCompare);
		}
		
		throw new IllegalStateException("Unsupported class for matching");
	}
	
	public boolean matchValue(String valueToCompare) {
		if(this.hasCondition()) {
			throw new IllegalStateException("Comparison not possible with Strings");
		}
		
		if(this.value.equals(valueToCompare)) {
			return true;
		}
		return false;
	}
	
	public boolean matchValue(double valueToCompare) {
		if(!this.hasCondition()) {
			throw new IllegalStateException("Comparison not possible without condition");
		}
		
		if(this.getCondition().equals("<=")) {
			return valueToCompare<=this.getDoubleValue();
		}else if(this.getCondition().equals(">=")) {
			return valueToCompare>=this.getDoubleValue();
		}else if(this.getCondition().equals("<")) {
			return valueToCompare<this.getDoubleValue();
		}else if(this.getCondition().equals(">")) {
			return valueToCompare>this.getDoubleValue();
		}
		
		throw new IllegalStateException("Broken comparison!");
	}
	
	@Override
    public int compareTo(MappingValue obj) {
        return rawValue.compareTo(obj.getRawValue());
    }
	
	@Override
	public String toString() {
		return this.rawValue;
	}
	
}
