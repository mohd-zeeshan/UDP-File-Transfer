FILES:
	1. /iteration2 : project folder to load via eclipse.
	2. UML_Class_Diagram.jpg : UML class diagram


SETUP INSTRUCTIONS:
	
	*** LOADING THE PROJECT:

		1. Unzip "iteration1_deliverables.zip" file, if you haven't already.
		2. Open eclipse-java.
		3. Select File > Import.
		4. Select General > Existing Projects into Workspace.
			Make sure you have no other project named "iteration1" in your workspace. If a project called "/iteration1" already exists, either delete it or rename it.
		5. Click "Next". Then browse through and select "iteration1_deliverables/iteration1" folder under "Select root directory".
		6. Click "Finish".
		

	*** RUNNING THE PROGRAM:
	
		1. For running multiple programs simultaneously in Eclipse, right click on each source file (Client.java, Host.java and Server.java) and choose: "Run As >> Java Application". (Ensure that each program runs in a separate console window.)
			See https://stackoverflow.com/a/5453914
		2. Run "Server.java" (Under "tftp" package).
		3. Run "ErrorSimulator.java" (Optional, since we are not doing any error simulation at this point in time)
		4. Run "Client.java" (Under "tftp" package)
		5. Enter '1' to send an RRQ OR '2' to send an WRQ.
		6. Type and enter the filename you want to read (e.g. enter 'server_big.txt' for RRQ or 'client_big.txt' for WRQ).
			***NOTE: 
			For RRQ: the file must reside in "test_files/server" folder. (e.g 'server_big.txt')
			For WRQ: the file must reside in "test_files/client" folder. (e.g 'client_big.txt')
		7. For RRQ: The file now should be transferred to the 'test_files/client' folder from server.
		   For WRQ: The file should be transferred to the 'test_files/server' folder from client.
		8. On the server console enter 'exit' to shut down server OR enter 'c' to continue.
			Note: Must enter 'c' to continue transferring file.
		9. On the client console either enter 'exit' to shut down client or repeat step 5 to 8 to continue transferring files.
		
Breakdown of responsibilities:
	Ankur Mishra : Design, Coding
	Dhrubomoy Das Gupta: Design, Coding, UML diagram.
	Goksu Ceylan: Design, Coding
	Vadim Yastrebov: Design, Coding
	Zeeshan Sh: Design, Coding, UCM diagram
	
		
		
