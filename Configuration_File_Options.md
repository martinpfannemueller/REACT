# REACT Configuration file options

## MAPE Component

REACT offers the following configuration options for setting up and running a MAPE component:

* **name** (String, mandatory): The name of the component.
* **logicType** (String, mandatory): "Monitor", "Analyzer", "Planner", "Executor"
* **consulHost** (String): IP-Address or hostname of the [Consul](https://www.consul.io/) host for registering this component at Consul and for finding its successor or knowledge components using Consul as well.
* **port** (Integer): Possibility for manually specifying the port number of this component. Otherwise a free random port ist used.
* **successorName** (String): Name of the successor component. This is also used for finding the successor component using Consul or via multicast DNS.
* **successorIP** (String): Manual setup of the successor using an IP-Address.
* **successsorPort** (Integer): Port of the successor running on the host with the manually set up IP-Address.
* **knowledgeName** (String): Name of the knowledge instance that this component should connect to. This name is also used for finding the component using Consul or via multicast DNS.
* **knowledgeIP** (String): Manual setup of the knowledge component using an IP-Address.
* **knowledgePort** (Integer): Port of the knowledge component running on the host with the manually set up IP-Address.
* **networkInterface** (String): IP-Address of the network interface that should be used in case of multiple network cards. The default behavior is to use the first available network card.
* **monitoringStrategy** (String, only for monitor): "aggregation" or "windowing". The default strategy only parsing the JSON input is used otherwise.
* **logInputOutput** (Boolean String): Only used for debugging. This prints the input and outputs of this component to the console.
* **printSendingTime** (Boolean String): Only used for evaluation. This lets the component print a timestamp when it sends away its result.

The filename of the config file of a MAPE component must be like this `manta.ALElement-<YOUR_NAME>.cfg`

## Knowledge Component

REACT offers the following configuration options for setting up and running a knowledge component:

* **name** (String, mandatory): The name of the component.
* **consulHost** (String): IP-Address or hostname of the [Consul](https://www.consul.io/) host for registering this component at Consul.
* **port** (Integer): Possibility for manually specifying the port number of this component. Otherwise a free random port ist used.
* **networkInterface** (String): IP-Address of the network interface that should be used in case of multiple network cards. The default behavior is to use the first available network card.
* **claferFile** (String): Path to an AOS file (Clafer specification). This file is automatically loaded on startup of the component.
* **componentModelFile** (String): Path to a TSS file (UML XML). This file is automatically loaded on startup of the component.

The filename of the config file of a Knowledge component must be like this `manta.KnowledgeElement-<YOUR_NAME>.cfg`

## Sensor Component

REACT offers the following configuration options for setting up and running a sensor component:

* **name** (String, mandatory): The name of the component.
* **consulHost** (String): IP-Address or hostname of the [Consul](https://www.consul.io/) host for registering this component at Consul and for finding its successor using Consul as well.
* **port** (Integer): Possibility for manually specifying the port number of this component. Otherwise a free random port ist used.
* **successorName** (String): Name of the successor component. This is also used for finding the successor component using Consul or via multicast DNS.
* **successorIP** (String): Manual setup of the successor using an IP-Address.
* **successsorPort** (Integer): Port of the successor running on the host with the manually set up IP-Address.
* **networkInterface** (String): IP-Address of the network interface that should be used in case of multiple network cards. The default behavior is to use the first available network card.
* **printSendingTime** (Boolean String): Only used for evaluation. This lets the component print a timestamp when it sends away its result.

The filename of the config file of a Sensor component must be like this `manta.Sensor-<YOUR_NAME>.cfg`