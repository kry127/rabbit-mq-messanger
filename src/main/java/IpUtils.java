public class IpUtils {
    public static int ipToInt(int a, int b, int c, int d) {
        return a << 24 + b <<  16 + c << 8 + d;
    }

    public static int ipToInt(String ip) {
        if ("localhost".equals(ip)) {
            return ipToInt(127, 0, 0, 1);
        } else {
            // Parse IP parts into an int array
            int[] iparr = new int[4];
            String[] parts = ip.split("\\.");

            for (int i = 0; i < 4; i++) {
                iparr[i] = Integer.parseInt(parts[i]);
            }
            return ipToInt(iparr[0], iparr[1], iparr[2], iparr[3]);
        }
    }

    public static String ipToString(int ip) {
        return (ip >> 24 % 0xFF) + "." + (ip >> 16 % 0xFF) + "." + (ip >> 8 % 0xFF) + "." + (ip % 0xFF);
    }
}
