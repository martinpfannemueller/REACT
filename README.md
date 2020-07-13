# REACT - Runtime Environment for Adapting Communication SysTems

This repository contains the source code of REACT presented in the full paper titled *REACT: A Model-Based Runtime Environment for Adapting Communication Systems* and the demo with the title *Enhancing a Communication System with Adaptive Behavior using REACT*.

## Repository Structure

```
📦REACT
 ┣ 📂bndtools: Contains Bndtools workspace
 ┃ ┣ 📂ALElement
 ┃ ┃ ┣ 📜REACT_Example.bndrun: Run Configuration for running REACT inside the IDE with dummy example
 ┃ ┃ ┣ 📜REACT.bndrun: Run Configuration for running REACT inside the IDE
 ┃ ┃ ┣ 📜REACT_Export.bndrun: Run Configuration for exporting REACT as a jar
 ┃ ┃ ┣ 📜REACT_only_Knowledge.bndrun: Run Configuration for running the knowledge inside the IDE
 ┃ ┃ ┣ 📜REACT_only_Knowledge_Export.bndrun: Run Configuration for exporting the knowledge service as a jar
 ┃ ┃ ┗ 📜REACT_only_MAPE.bndrun: Run Configuration for running the only the MAPE loop inside the IDE
 ┃ ┣ 📂Effector: Contains dummy effector as OSGi module
 ┃ ┣ 📂Knowledge: Contains knowledge service
 ┃ ┣ 📂Sensor: Contains sensor service
 ┃ ┣ 📂SensorClient: Contains dummy sensor as OSGi module
 ┃ ┣ 📂cnf: Contains configurations and dependencies with added OSGi manifests
 ┃ ┣ 📂config: Contains config files for instantiating REACT
 ┃ ┃ ┃ ┣ 📂example_knowledge: Example specifications
 ┃ ┃ ┃ ┃ ┣ 📜AOS.cfr
 ┃ ┃ ┃ ┃ ┗ 📜TSS.uml
 ┃ ┃ ┃ ┣ 📜react.ALElement-A.cfg
 ┃ ┃ ┃ ┣ 📜react.ALElement-E.cfg
 ┃ ┃ ┃ ┣ 📜react.ALElement-M.cfg
 ┃ ┃ ┃ ┣ 📜react.ALElement-P.cfg
 ┃ ┃ ┃ ┣ 📜react.Effector-Dummy.cfg
 ┃ ┃ ┃ ┣ 📜react.KnowledgeElement-1.cfg
 ┃ ┃ ┃ ┣ 📜react.Sensor-1.cfg
 ┃ ┃ ┃ ┗ 📜react.SensorClient-Dummy.cfg
 ┃ ┃ ┣ 📜build.bnd
 ┃ ┃ ┣ 📜cardygan.maven
 ┃ ┃ ┣ 📜central.maven
 ┃ ┃ ┣ 📜eclipse-releases.maven
 ┃ ┃ ┗ 📜local.maven: Maven files specify dependencies from different sources
 ┣ 📂core: Contains logic and structure of REACT
 ┃ ┣ 📂claferanalyzer
 ┃ ┣ 📂claferexecutor
 ┃ ┣ 📂clafermonitor
 ┃ ┣ 📂claferplanner
 ┃ ┣ 📂knowledge
 ┃ ┣ 📂structure
 ┃ ┗ 📂util
 ┗ 📂idl: Separate IDL project for specifying interfaces
```

## Setup Development Environment

1. Download and install the [Eclipse IDE for Java Developers](https://www.eclipse.org/downloads/packages/).
2. Install Bndtools according to their website [here](https://bndtools.org/installation.html).
3. Optional: Install [Eclipse Papyrus](https://www.eclipse.org/papyrus/download.html) if you want to edit the Target System Specification (UML) inside the graphical editor.
4. Run Eclipse and create a first workspace named, e.g., `REACT_Logics`. Import the `core` and `idl` folder using the import wizard for importing existing Maven projects.
5. Run `clean install` using Maven for the idl project first, and then for the core project. This installs the idl and core jars in your local Maven repository which will be used by Bndtools later.
6. Create a separate workspace named, e.g., `REACT_Bndtools`. Use the import wizard for importing an existing Bndtools workspace. Select the `bndtools` folder from the repository.
7. Download Clafer 0.4.5 from [here](https://gsd.uwaterloo.ca/clafer-tools-binary-distributions.html) and put it to a memorable place.
8. Optional: Add the extracted Clafer folder to your path.

## Running REACT

Below you can find an example output of running REACT using the `REACT_Example.bndrun` run configuration inside of Eclipse in the `REACT_Bndtools` workspace.  Here, the multicast DNS mode is used for setting up the connections between the different components. This configuration also loads a dummy sensor end effector. The dummy sensor sends its data after 10 seconds. The last line shows the resulting adaptation decision for instantiating an instance of `ServerLauncher`. The specifications are taken from the supplementary material [here](https://github.com/martinpfannemueller/ACSOS2020-Supplementary-Material/tree/master/Enhancing_a_Communication_System_with_Adaptive_Behavior_using_REACT/configs).

---
**NOTE**: When you first run this run configuration without completing step 8 from above, you will get an exception stating, that the Clafer binary could not be found. In this case, you have to provide the environment variable `CLAFER_EXEC_PATH` to the run configuration inside Eclipse with an absolute path to the Clafer executable.

---

```
____________________________
Welcome to Apache Felix Gogo

g! Executor-1 running with Multicast DNS on port 64184
Executor-1 uses 134.155.X.Y as network interface (automatic selection)
Analyzer-1 running with Multicast DNS on port 64186
Analyzer-1 uses 134.155.X.Y as network interface (automatic selection)
Planner-1 running with Multicast DNS on port 64188
Planner-1 uses 134.155.X.Y as network interface (automatic selection)
Monitor-1 running with Multicast DNS on port 64190
Monitor-1 uses 134.155.X.Y as network interface (automatic selection)
Successfully read Clafer specification: ../cnf/config/example_knowledge/AOS.cfr
Successfully read Component specification: ../cnf/config/example_knowledge/TSS.uml
Knowledge running with multicast DNS on port 64193
Knowledge uses 134.155.X.Y as network interface (automatic selection)
Sensor-1 running with multicast DNS on port 64195
Sensor-1 uses 134.155.X.Y as network interface (automatic selection)
Running Monitor with logic class logicElements.monitor.ClaferMonitor and bundle react.clafermonitor
Planner-1 connected to successor with endpoint Executor-1:default -p 64184 -h 134.155.X.Y
Analyzer-1 connected to successor with endpoint Planner-1:default -p 64188 -h 134.155.X.Y
Monitor-1 connected to successor with endpoint Analyzer-1:default -p 64186 -h 134.155.X.Y
Analyzer-1 connected to knowledge with endpoint Knowledge:default -p 64193 -h 134.155.X.Y
Executor-1 connected to knowledge with endpoint Knowledge:default -p 64193 -h 134.155.X.Y
Planner-1 connected to knowledge with endpoint Knowledge:default -p 64193 -h 134.155.X.Y
Running Planner with logic class logicElements.planner.ClaferPlanner and bundle react.claferplanner
Running Analyzer with logic class logicElements.analyzer.ClaferAnalyzer and bundle react.claferanalyzer
Planner-1 connected to knowledge with endpoint Knowledge:default -p 64193 -h 134.155.X.Y
Analyzer-1 connected to knowledge with endpoint Knowledge:default -p 64193 -h 134.155.X.Y
Running Executor with logic class logicElements.executor.ClaferExecutor and bundle react.claferexecutor
Executor-1 connected to knowledge with endpoint Knowledge:default -p 64193 -h 134.155.X.Y
ServerLauncher
```

## Additional Documentation

[Configuration File Options](Configuration_File_Options.md): Contains a complete list of all options for instantiating REACT's different components.

For generating language bindings for REACT's interfaces see the documentation of [ZeroC Ice](https://doc.zeroc.com/). As an example, for (re-) creating the Java bindings run the following command inside the `idl` folder with the according Slice generator (in this case the generator for [Java](https://zeroc.com/downloads/ice/3.7/java)) installed on your system: `slice2java src/main/resources/Manta.ice --output-dir generated/`

---
**NOTE** : You will encounter the term *Manta* several times in the code as this was the internal code name of REACT.

---

## License

REACT is licenced under the Apache 2.0 license.
