import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class vpnList {
    Set<String> addressesDNS = new HashSet<>();
    Set<String> addressesIP = new HashSet<>();
    DNSResolver resolver = new DNSResolver();
    String pathToStorage;

    public void init(String pathToStorage) {
        this.pathToStorage = pathToStorage;
        try {
            BufferedReader fReader = new BufferedReader((new FileReader(pathToStorage)));
            String line;
            while ((line = fReader.readLine()) != null)
                addressesDNS.add(line);
            updateIpList();
            fReader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int updateIpList() {
        addressesIP.clear();
        int result = 0;
        Set<String> flush = new HashSet<>();
        Pattern AS = Pattern.compile("^[A-Z]{2,}\\d+$");
        for (String domainName : addressesDNS) {
            if (domainName.contains("/")) {
                System.out.println(domainName);
                addressesIP.add(domainName);
            }
            else if (AS.matcher(domainName).matches()) {
                System.out.println(domainName);
                Set<String> setAS = resolver.getIpByAS(domainName);
                for (String setA : setAS) {
                    System.out.println("\t" + setA);
                }
                addressesIP.addAll(setAS);
            }
            else {
                Set<String> IP = resolver.getIpByName(domainName);
                System.out.println(domainName);
                if (IP != null) {
                    for (String s : IP) {
                        addressesIP.add(s + "/32");
                        System.out.println("\t" + s + "/32");
                    }
                } else {
                    flush.add(domainName);
                    result = -1;
                }
            }
        }
        addressesDNS.removeAll(flush);
        return result;
    }

    public int addNewIp(String domainName) {
        if (domainName.contains("/")) {
            addressesDNS.add(domainName);
            addressesIP.add(domainName);
            System.out.println(domainName);
            return 0;
        }
        addressesDNS.add(domainName);
        Set<String> IP = resolver.getIpByName(domainName);
        System.out.println(domainName);
        if (IP != null) {
            for (String s : IP) {
                addressesIP.add(s + "/32");
                System.out.println("\t" + s + "/32");
            }
        }
        return 0;
    }

    public int delIp(String domainName) {
        if (domainName.contains("/")) {
            addressesDNS.remove(domainName);
            addressesIP.remove(domainName);
            System.out.println(domainName);
            return 0;
        }
        addressesDNS.remove(domainName);
        Set<String> IP = resolver.getIpByName(domainName);
        System.out.println(domainName);
        if (IP != null) {
            for (String s : IP) {
                addressesIP.remove(s + "/32");
                System.out.println("\t" + s + "/32");
            }
        }
        return 0;
    }

    public int addNewAS(String domainName) {
        Pattern AS = Pattern.compile("^AS[0-9]+$");
        if (!AS.matcher(domainName).matches())
            return -1;
        addressesDNS.add(domainName);
        Set<String> IP = resolver.getIpByAS(domainName);
        System.out.println(domainName);

        if (IP != null) {
            for (String s : IP) {
                System.out.println("\t" + s);
            }
        }
        assert IP != null;
        addressesIP.addAll(IP);
        return 0;
    }

    public int delAS(String domainName) {
        Pattern AS = Pattern.compile("^AS[0-9]+$");
        if (!AS.matcher(domainName).matches())
            return -1;
        addressesDNS.remove(domainName);
        Set<String> IP = resolver.getIpByAS(domainName);
        System.out.println(domainName);

        if (IP != null) {
            for (String s : IP) {
                System.out.println("\t" + s);
            }
        }
        assert IP != null;
        addressesIP.removeAll(IP);
        return 0;
    }


    public void addDomainName(String domainName) {
        addressesDNS.add(domainName);
    }

    public void delDomainName(String domainName) {
        addressesDNS.remove(domainName);
    }

}
