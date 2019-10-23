# AIAD

### How to run

IntelliJ : In out/production/<project_folder_name>

1. GUI: ```java jade.Boot - gui```
2. Technician:

    - One: ```java jade.Boot -container <agent_name>:agents.Technician```
    - Multiple: ```java jade.Boot -container "<agent1_name>:agents.Technician ; <agent2_name>:agents.Technician ; ..."```
 
3. Client: 
    - One: ```java jade.Boot -container <client_name>:agents.Client```
    - Multiple: ```java jade.Boot -container "<client1_name>:agents.Client ; <client2_name>:agents.Client ; ..."```
    
    
Agent with params: ```java jade.Boot -container <agent_name>:<agent_class>(<agent_param1> <agent_param2> ...)```