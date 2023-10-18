import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Worker {
    vpnList VPN = new vpnList();
    Set<String> routeForAdd = new HashSet<>();
    Set<String> routeForDel = new HashSet<>();
    public boolean inWork = false;

    public void init(String pathToStorage) {
        VPN.init((pathToStorage));
    }

    public void checkConsistency() {
        ArrayList<String> currentRoutes = new ArrayList<>();
        ProcessBuilder pb = new ProcessBuilder();
        routeForAdd.clear();
        routeForDel.clear();
        pb.command("bash" , "-c", "sshpass -p 'password' ssh router_user@router_ip show ip route");
        try {
            Process exec = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("OpenVPN1")) {
                    currentRoutes.add(line.split("\s")[0]);
                }
                /*if (!line.contains("192.168.5")){
                    String subnet = line.split(" ")[0];
                    if (!subnet.contains("/"))
                        subnet  += "/32";
                    currentRoutes.add(subnet);
                }*/
            }
            for (String currentRoute : currentRoutes) {
                if (!VPN.addressesIP.contains(currentRoute))
                    routeForDel.add("no ip route " + currentRoute + " OpenVPN1");
            }
            for (String addressIP : VPN.addressesIP) {
                if (!currentRoutes.contains(addressIP))
                    routeForAdd.add("ip route " + addressIP + " OpenVPN1 auto");
            }
            if (routeForDel.size() > 0){
                System.out.println("Delete:");
            }
            for (String route : routeForDel) {
                ProcessBuilder pbHelper = new ProcessBuilder();
                pbHelper.command("bash" , "-c", "sshpass -p 'password' ssh router_user@router_ip " + route);
                Process execHelper = pbHelper.start();
                Thread.sleep(100);
                System.out.println(route);
            }
            if (routeForAdd.size() > 0){
                System.out.println("Add:");
            }
            for (String route : routeForAdd) {
                ProcessBuilder pbHelper = new ProcessBuilder();
                pbHelper.command("bash" , "-c", "sshpass -p 'password' ssh router_user@router_ip " + route);
                Process execHelper = pbHelper.start();
                Thread.sleep(100);
                System.out.println(route);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public String commandExecute(String command) {
        inWork = true;


        if (command.equals("show")) {
            String result = "";
            for (String address : VPN.addressesDNS) {
                result += address + "\n";
            }
            inWork = false;
            return result;
        }

        if (!command.contains(" ")) {
            inWork = false;
            return "Invalid command";
        }


        try{
            String domainName = command.split((" "))[1];
            command = command.split((" "))[0];
            int result = 1;
            if (command.equals("add")) {
                result = VPN.addNewIp(domainName);
            }
            else if (command.equals("del")) {
                result = VPN.delIp(domainName);
            }
            else if (command.equals("addAS")) {
                result = VPN.addNewAS(domainName);
            }
            else if (command.equals("delAS")) {
                result = VPN.delAS(domainName);
            }

            else {
                inWork = false;
                return "Unknown command!";
            }

            if (result == -1) {
                inWork = false;
                return "WTF?";
            }
            if (result == 0){
                FileWriter writer = new FileWriter(VPN.pathToStorage, false);
                for (String addressDNS : VPN.addressesDNS) {
                    writer.write(addressDNS + "\n");
                }
                writer.close();
            }

            checkConsistency();
        }
        catch (Exception e) {
            inWork = false;
            return "ERROR:\n" + e;
        }
        inWork = false;
        return "Command executed";
    }
}
