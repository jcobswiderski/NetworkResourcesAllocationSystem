import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class NetworkNode extends Thread {
    public NetworkNode(Socket clientSocket, String[] args) {
        String ident = "";
        int tcpport = 0;
        String outputIp = "localhost";
        int outputPort = 0; // ?

        Map<String, Integer> availableResources = new HashMap<String, Integer>(); // <nazwa zasobu, licznosc zasobu>
        Map<String, String> occupiedResources = new HashMap<String, String>(); // <id klienta, seria zasobow>

        for (int i = 0; i < args.length; i++) {
            if(args[i].equals("-ident")) {
                ident = args[i+1];
                i++;
            } else if(args[i].equals("-tcpport")) {
                tcpport = Integer.valueOf(args[i+1]);
                i++;
            } else if(args[i].equals("-gateway")) {
                outputIp = args[i+1].split(":")[0];
                outputPort = Integer.valueOf(args[i+1].split(":")[1]);
                i++;
            } else {
                availableResources.put(args[i].split(":")[0], Integer.valueOf(args[i].split(":")[1]));
            }
        }

        try {
            if(outputIp !="" && outputPort != 0) {
                System.out.println("NetworkNode {id:" + ident + " | accessPort:" + tcpport + " | parent:" + outputIp + ":" + outputPort + "} has started...");
            } else {
                System.out.println("NetworkNode {id:" + ident + " | accessPort:" + tcpport + " | parent: NONE} has started...");
            }
            displayResources(availableResources, occupiedResources);

            ServerSocket accessSocket = new ServerSocket(tcpport);


            while(true) {
                clientSocket = accessSocket.accept();

                System.out.println("Client connected!");
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String clientRequest;
                if ((clientRequest = in.readLine()) != null) {
                    String[] data = clientRequest.split(" "); // ["id", "A:10", "B:20", ..., "terminate"]

                    if(data[data.length-1].equals("TERMINATE")) {
                        System.out.print("CLIENT " + data[0] + " returning resources");

                        if(occupiedResources.containsKey(data[0])) {
                            String[] resourcesToReturn = occupiedResources.get(data[0]).split(" ");
                            for(int i=0; i< resourcesToReturn.length; i++) {
                                String resourceName = resourcesToReturn[i].split(":")[0];
                                String resourceCount = resourcesToReturn[i].split(":")[1];

                                availableResources.put(resourceName, availableResources.get(resourceName) + Integer.valueOf(resourceCount));
                                System.out.print(" " + resourceName + ":" + resourceCount);
                            }
                            occupiedResources.remove(data[0]);
                        }
                        System.out.println("\nResources successfully returned!");
                        displayResources(availableResources, occupiedResources);
                        out.println("");
                        continue;
                    }

                    System.out.println("Resources request from client with id:" + data[0]);

                    boolean canAssign = true;
                    for(int i=1; i<data.length; i++) {
                        String resourceName = data[i].split(":")[0];
                        int resourceCount = Integer.valueOf(data[i].split(":")[1]);

                        if(availableResources.containsKey(resourceName)) {
                            if(availableResources.get(resourceName) < resourceCount) {
                                canAssign = false;
                                break;
                            }
                        } else {
                            canAssign = false;
                            break;
                        }
                    }

                    if(canAssign) {
                        for(int i=1; i<data.length; i++) {

                            String resourceName = data[i].split(":")[0];
                            int resourceCount = Integer.valueOf(data[i].split(":")[1]);

                            availableResources.put(resourceName, availableResources.get(resourceName)-resourceCount);

                            if(occupiedResources.containsKey(data[0]) && occupiedResources.get(data[0])!=null) {
                                occupiedResources.put(data[0], occupiedResources.get(data[0]) + " " + resourceName + ":" + resourceCount);
                            } else {
                                occupiedResources.put(data[0], resourceName + ":" + resourceCount);
                            }

                            out.println(resourceName + ":" + resourceCount + ":" + outputIp + ":" + tcpport);

                        }
                        System.out.println("Resources successfully allocated!");
                        displayResources(availableResources, occupiedResources);
                        out.println("");
                    } else {
                        System.out.println("Resources allocation couldn't be completed!\n");
                        out.println("FAILED");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void displayAvailableResources(Map<String, Integer> map) {
        System.out.print("Available resources: ");
        map.forEach((key, value) -> System.out.print(key + ":" + value + " "));
        System.out.println();
    }

    public static void displayOccupiedResources(Map<String, String> map) {
        System.out.print("Occupied resources: ");
        map.forEach((key, value) -> System.out.print(key + "{" + value + "} "));
        System.out.println();
    }

    public static void displayResources(Map<String, Integer> available, Map<String, String> occupied) {
        System.out.println();
        displayAvailableResources(available);
        displayOccupiedResources(occupied);
        System.out.println();
    }

    public static void main(String... args) {
        Socket clientSocket = null;
        new NetworkNode(clientSocket, args).start();
    }
}
