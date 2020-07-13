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

package util.clafer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import util.clafer.impl.AOSParser;

public class AOSParserTest {

	@Test
	public void testAOSParser1() {
		
		final String str = "ServerLauncher 0..1\n" + 
				"Constants 1..1\n" + 
				"    rtThreshold -> integer 1..1 = 75\n" + 
				"abstract Context 1..1\n" + 
				"    servers -> integer 1..1\n" + 
				"    maxServers -> integer 1..1\n" + 
				"    responseTime -> integer 1..1\n" + 
				"\n" + 
				"ExtraServers 0..1\n" + 
				"HighRT 0..1\n" + 
				"[ if Context.servers < Context.maxServers then one ExtraServers	else no ExtraServers\n" + 
				"  if Context.responseTime >= Constants.rtThreshold	then one HighRT	else no HighRT \n" + 
				"  if HighRT && ExtraServers then one ServerLauncher ]";
		
		assertEquals(1, AOSParser.getAbstractClafers(str).size());
	}
	
	@Test
	public void testAOSParser2() {
		
		final String str = "abstract Host\n" + 
				"    mac -> MacAddr\n" + 
				"    ipAddress -> IpAddr\n" + 
				"    port -> integer\n" + 
				"\n" + 
				"abstract Switch\n" + 
				"    switchId -> SwitchId\n" + 
				"\n" + 
				"abstract DestinationHost\n" + 
				"    [ all disj f1;f2:flowRules | f1.outPort != f2.outPort && f1.inPort != f2.inPort && f1.inPort = f2.outPort && f1.srcMac != f2.srcMac && f1.dstMac != f2.dstMac && f1.srcMac = f2.dstMac && f1.srcIP != f2.srcIP && f1.dstIP != f2.dstIP && f1.srcIP = f2.dstIP  ]\n" + 
				"    [ all disj df1;df2:duplicationFlowRules | df1.outPort != df2.outPort && df1.inPort != df2.inPort && df1.inPort = df2.outPort && df1.srcMac != df2.srcMac && df1.dstMac != df2.dstMac && df1.srcMac = df2.dstMac && df1.srcIP != df2.srcIP && df1.dstIP != df2.dstIP && df1.srcIP = df2.dstIP  ]\n" + 
				"    [ distance.dref > 15 => no duplicationFlowRules ]\n" + 
				"    [ distance.dref <= 15 => # duplicationFlowRules = 2 ]\n" + 
				"    mac -> MacAddr\n" + 
				"    ipAddress -> IpAddr\n" + 
				"    port -> integer\n" + 
				"    flowRules -> FlowRule 2\n" + 
				"    duplicationFlowRules -> DuplicationFlowRule 0..2\n" + 
				"    distance -> integer ?\n" + 
				"\n" + 
				"abstract MacAddr\n" + 
				"abstract IpAddr\n" + 
				"abstract SwitchId\n" + 
				"\n" + 
				"FlowRule *\n" + 
				"    [this in DestinationHost.flowRules]\n" + 
				"    [ this.switchId = Switch.switchId ]\n" + 
				"    [ this.inPort != this.outPort ]\n" + 
				"    [ this.inPort = Host.port || this.inPort = DestinationHost.port]\n" + 
				"    [ this.outPort = DestinationHost.port || this.outPort = Host.port]\n" + 
				"    [ this.srcMac != this.dstMac ]\n" + 
				"    [ this.srcMac = Host.mac || this.srcMac = DestinationHost.mac ]\n" + 
				"    [ this.dstMac = DestinationHost.mac || this.dstMac = Host.mac ]\n" + 
				"    [ this.srcIP != this.dstIP ]\n" + 
				"    [ this.srcIP = Host.ipAddress || this.srcIP = DestinationHost.ipAddress ]\n" + 
				"    [ this.dstIP = DestinationHost.ipAddress || this.dstIP = Host.ipAddress ]\n" + 
				"    switchId -> SwitchId\n" + 
				"    inPort -> integer\n" + 
				"    outPort -> integer\n" + 
				"    srcMac -> MacAddr\n" + 
				"    dstMac -> MacAddr\n" + 
				"    srcIP -> IpAddr\n" + 
				"    dstIP -> IpAddr\n" + 
				"\n" + 
				"DuplicationFlowRule *\n" + 
				"    [ this in DestinationHost.duplicationFlowRules]\n" + 
				"    [ this.switchId = Switch.switchId ]\n" + 
				"    [ this.inPort != this.outPort ]\n" + 
				"    [ this.inPort = Host.port || this.inPort = DestinationHost.port + 1]\n" + 
				"    [ this.outPort = DestinationHost.port + 1 || this.outPort = Host.port]\n" + 
				"    [ this.srcMac != this.dstMac ]\n" + 
				"    [ this.srcMac = Host.mac || this.srcMac = DestinationHost.mac ]\n" + 
				"    [ this.dstMac = DestinationHost.mac || this.dstMac = Host.mac ]\n" + 
				"    [ this.srcIP != this.dstIP ]\n" + 
				"    [ this.srcIP = Host.ipAddress || this.srcIP = DestinationHost.ipAddress ]\n" + 
				"    [ this.dstIP = DestinationHost.ipAddress || this.dstIP = Host.ipAddress ]\n" + 
				"    switchId -> SwitchId\n" + 
				"    inPort -> integer\n" + 
				"    outPort -> integer\n" + 
				"    srcMac -> MacAddr\n" + 
				"    dstMac -> MacAddr\n" + 
				"    srcIP -> IpAddr\n" + 
				"    dstIP -> IpAddr";
		
		assertEquals(6, AOSParser.getAbstractClafers(str).size());
	}
	
	@Test
	public void testAOSParser3() {
		
		final String str = "HighRT ?\n" + 
				"ExtraServers ?\n" + 
				"Underloaded ? \n" + 
				"MoreThanOneActiveServer ?\n" + 
				"DimmerDecreasable ?\n" + 
				"DimmerIncreasable ?\n" + 
				"\n" + 
				"[\n" + 
				"	if Context.basicResponseTime.dref >= Constants.rtThreshold.dref || Context.optResponseTime.dref >= Constants.rtThreshold.dref\n" + 
				"		then\n" + 
				"			one HighRT\n" + 
				"		else\n" + 
				"			no HighRT\n" + 
				"\n" + 
				"	if Context.servers.dref < Context.maxServers.dref\n" + 
				"		then\n" + 
				"			one ExtraServers\n" + 
				"		else\n" + 
				"			no ExtraServers\n" + 
				"\n" + 
				"	if Context.averageUtilization.dref < 30\n" + 
				"		then\n" + 
				"			one Underloaded\n" + 
				"		else\n" + 
				"			no Underloaded\n" + 
				"\n" + 
				"	if Context.activeServers.dref > 1\n" + 
				"		then \n" + 
				"			MoreThanOneActiveServer\n" + 
				"		else\n" + 
				"			no MoreThanOneActiveServer\n" + 
				"\n" + 
				"\n" + 
				"	if (1 + ( (Context.dimmer.dref + (-Constants.dimmerMargin.dref)) * (Constants.numberOfDimmerLevels.dref + (-1) ) / (100 + (-(2 * Constants.dimmerMargin.dref))))) > 1\n" + 
				"		then \n" + 
				"			DimmerDecreasable\n" + 
				"		else\n" + 
				"			no DimmerDecreasable\n" + 
				"\n" + 
				"	if Context.dimmer.dref + Constants.dimmerStep.dref < 100\n" + 
				"		then\n" + 
				"			DimmerIncreasable\n" + 
				"		else\n" + 
				"			no DimmerIncreasable\n" + 
				"\n" + 
				"\n" + 
				"	if HighRT && ExtraServers\n" + 
				"		then\n" + 
				"			Adaptation1.AddServer && Adaptation1.AddServer.waitingTime = 60 && Adaptation2.DecreaseDimmer && Adaptation2.DecreaseDimmer.waitingTime = 100 && Adaptation2.DecreaseDimmer.condition = Condition_basicResponseTime_hte_75_OR_optResponseTime_hte_75\n" + 
				"		else\n" + 
				"			no Adaptation1.AddServer && no Adaptation2.DecreaseDimmer\n" + 
				"\n" + 
				"	if Underloaded && (MoreThanOneActiveServer || DimmerIncreasable)\n" + 
				"		then\n" + 
				"			if Underloaded && MoreThanOneActiveServer\n" + 
				"				then\n" + 
				"					Adaptation1.RemoveServer && Adaptation1.RemoveServer.waitingTime = 60 && Adaptation2.IncreaseDimmer && Adaptation2.IncreaseDimmer.waitingTime = 100 && Adaptation2.IncreaseDimmer.condition = Condition_averageUtilization_lt_30\n" + 
				"				else\n" + 
				"					if Underloaded && DimmerIncreasable\n" + 
				"						then\n" + 
				"							Adaptation1.IncreaseDimmer\n" + 
				"						else \n" + 
				"							no Adaptation1.IncreaseDimmer\n" + 
				"		else\n" + 
				"			no Adaptation1.RemoveServer\n" + 
				"]\n" + 
				"\n" + 
				"NoOp\n" + 
				"\n" + 
				"abstract Condition\n" + 
				"\n" + 
				"Condition_basicResponseTime_hte_75_OR_optResponseTime_hte_75 : Condition\n" + 
				"Condition_averageUtilization_lt_30 : Condition\n" + 
				"\n" + 
				"abstract Feature\n" + 
				"	waitingTime -> integer\n" + 
				"	condition -> Condition ?\n" + 
				"\n" + 
				"abstract Action 1..1\n" + 
				"	AddServer : Feature 0..1\n" + 
				"		[ no parent.RemoveServer ]\n" + 
				"		[ no parent.IncreaseDimmer ]\n" + 
				"		[ no parent.DecreaseDimmer ]\n" + 
				"		[ if waitingTime < 0\n" + 
				"			then \n" + 
				"				waitingTime = 0\n" + 
				"			else\n" + 
				"				NoOp\n" + 
				"		]\n" + 
				"	RemoveServer : Feature 0..1\n" + 
				"		[ no parent.AddServer ]\n" + 
				"		[ no parent.IncreaseDimmer ]\n" + 
				"		[ no parent.DecreaseDimmer ]\n" + 
				"		[ if waitingTime < 0\n" + 
				"			then \n" + 
				"				waitingTime = 0\n" + 
				"			else\n" + 
				"				NoOp\n" + 
				"		]\n" + 
				"	IncreaseDimmer : Feature 0..1\n" + 
				"		[ no parent.AddServer ]\n" + 
				"		[ no parent.RemoveServer ]\n" + 
				"		[ no parent.DecreaseDimmer ]\n" + 
				"		[ if waitingTime < 0\n" + 
				"			then \n" + 
				"				waitingTime = 0\n" + 
				"			else\n" + 
				"				NoOp\n" + 
				"		]\n" + 
				"	DecreaseDimmer : Feature 0..1\n" + 
				"		[ no parent.AddServer ]\n" + 
				"		[ no parent.RemoveServer ]\n" + 
				"		[ no parent.IncreaseDimmer ]\n" + 
				"		[ if waitingTime < 0\n" + 
				"			then \n" + 
				"				waitingTime = 0\n" + 
				"			else\n" + 
				"				NoOp\n" + 
				"		]\n" + 
				"\n" + 
				"\n" + 
				"abstract FirstAction : Action 1..1\n" + 
				"abstract SecondAction : Action 1..1\n" + 
				"\n" + 
				"Adaptation1 : FirstAction\n" + 
				"\n" + 
				"Adaptation2 : SecondAction\n" + 
				"\n" + 
				"abstract Context 1..1\n" + 
				"	dimmer -> integer 1..1\n" + 
				"	servers -> integer 1..1\n" + 
				"	activeServers -> integer 1..1\n" + 
				"	maxServers -> integer 1..1\n" + 
				"	totalUtilization -> integer 1..1\n" + 
				"	averageUtilization -> integer 1..1\n" + 
				"	basicResponseTime -> integer 1..1\n" + 
				"	optResponseTime -> integer 1..1\n" + 
				"\n" + 
				"Constants 1..1\n" + 
				"	dimmerMargin -> integer 1..1 = 10 // Division with 100\n" + 
				"	dimmerStep -> integer 1..1 = 20 // Division with 100\n" + 
				"	numberOfDimmerLevels -> integer 1..1 = 5\n" + 
				"	rtThreshold -> integer 1..1 = 75 // Division with 100\n" + 
				"	period -> integer 1..1 = 60";
		
		assertEquals(6, AOSParser.getAbstractClafers(str).size());
	}
	
}
