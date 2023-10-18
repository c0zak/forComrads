import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class DNSResolver {

    public Set<String> getIpByName(String domainName) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            Set<String> result = new HashSet<>();
            pb.command("bash" , "-c", "nslookup " + domainName);
//            pb.command("cmd", "/c", "nslookup " + domainName);
            Process exec = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Address:") && !line.contains("#53")) {
//                    result.add(line.split(" ")[1]);
                    line = line.split(" ")[1];
                    if (!line.contains(":"))
                        result.add(line);
                }
            }
            return result;
        }
        catch (Exception e) {
            return null;
        }
    }

    public Set<String> getIpByAS(String AS) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            Set<String> result = new HashSet<>();
            pb.command("bash" , "-c", " whois -h whois.radb.net -- '-i origin " + AS + "' | grep route:");
//            pb.command("cmd", "/c", "nslookup " + domainName);
            Process exec = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("route:", "");
                line = line.replaceAll("\s", "");
                line = line.replaceAll("\t", "");
                result.add(line);
            }
            return result;
        }
        catch (Exception e) {
            return null;
        }
    }

}
