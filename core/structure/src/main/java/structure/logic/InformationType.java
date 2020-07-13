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

package structure.logic;

public class InformationType {

	private String name;

	private InformationType(String name) {
		this.name = name;

	}
	public static InformationType Monitoring() {
		return new InformationType("Monitoring");
	}
	
	public static InformationType Analyzing() {
		return new InformationType("Analyzing");
	}
	
	public static InformationType Planning() {
		return new InformationType("Planning");
	}
	
	public static InformationType Executing() {
		return new InformationType("Executing");
	}
	
	public static InformationType Sensor() {
		return new InformationType("Sensor");
	}
	
	public static InformationType Effector() {
		return new InformationType("Effector");
	}
	
	@Override
	public String toString() {
		return name;
	}

}

