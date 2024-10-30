//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import java.io.File;
//import java.math.BigInteger;
//import java.util.ArrayList;
//import java.util.List;
//
//public class ShamirSecretSharingNoModulus {
//
//    public static void main(String[] args) {
//        try {
//            // Load and parse JSON file
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode root = mapper.readTree(new File("src/main/resources/input.json")); // Adjust filename accordingly
//
//            // Extract n and k
//            int n = root.get("keys").get("n").asInt();
//            int k = root.get("keys").get("k").asInt();
//
//            List<double[]> points = new ArrayList<>();
//
//            // Parse points and decode values
//            for (int i = 1; i <= n; i++) {
//                if (root.has(String.valueOf(i))) {
//                    int x = i;
//                    int base = root.get(String.valueOf(i)).get("base").asInt();
//                    String value = root.get(String.valueOf(i)).get("value").asText();
//                    double y = Double.parseDouble(new BigInteger(value, base).toString());
//                    points.add(new double[]{x, y});
//                }
//            }
//
//            // Sort points and select first k points
//            points.sort((a, b) -> Double.compare(a[0], b[0]));
//            List<double[]> selectedPoints = points.subList(0, k);
//
//            // Calculate the secret using Lagrange interpolation without modulus
//            double secret = lagrangeInterpolation(selectedPoints);
//            System.out.println("The constant term (secret) is: " + Math.round(secret));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Lagrange interpolation without modulus
//    private static double lagrangeInterpolation(List<double[]> points) {
//        double constantTerm = 0.0;
//
//        for (int i = 0; i < points.size(); i++) {
//            double xi = points.get(i)[0];
//            double yi = points.get(i)[1];
//            double term = yi;
//
//            for (int j = 0; j < points.size(); j++) {
//                if (i != j) {
//                    double xj = points.get(j)[0];
//                    term *= (-xj) / (xi - xj);
//                }
//            }
//
//            constantTerm += term;
//        }
//
//        return constantTerm;
//    }
//}















import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ShamirSecretSharing {

    public static void main(String[] args) {
        try {
            // Load and parse JSON file
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File("src/main/resources/input.json"));

            // Extract number of points (n) and minimum required points (k)
            int n = root.get("keys").get("n").asInt();
            int k = root.get("keys").get("k").asInt();

            // List to store the (x, y) points
            List<Point> points = new ArrayList<>();

            // Parse points and decode y values
            for (int i = 1; i <= n; i++) {
                if (root.has(String.valueOf(i))) {
                    int x = i;
                    int base = root.get(String.valueOf(i)).get("base").asInt();
                    String value = root.get(String.valueOf(i)).get("value").asText();
                    BigInteger y = new BigInteger(value, base);
                    points.add(new Point(x, y));
                }
            }

            // Calculate the constant term using Lagrange interpolation
            BigInteger secret = lagrangeInterpolation(points, k);
            System.out.println("The constant term (secret) is: " + secret);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Lagrange interpolation to find the polynomial's constant term
    private static BigInteger lagrangeInterpolation(List<Point> points, int k) {
        BigInteger secret = BigInteger.ZERO;
        BigInteger modulus = BigInteger.valueOf(257); // Change if necessary

        for (int i = 0; i < k; i++) {
            BigInteger xi = BigInteger.valueOf(points.get(i).x);
            BigInteger yi = points.get(i).y;

            BigInteger li = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i != j) {
                    BigInteger xj = BigInteger.valueOf(points.get(j).x);
                    BigInteger denominator = xi.subtract(xj).mod(modulus);

                    // Check if modInverse is possible
                    if (denominator.gcd(modulus).equals(BigInteger.ONE)) {
                        li = li.multiply(xj.negate()).multiply(denominator.modInverse(modulus)).mod(modulus);
                    } else {
                        System.out.println("Skipping point with denominator " + denominator + " due to lack of modular inverse.");
                        continue; // Skip this point in the interpolation
                    }
                }
            }

            secret = secret.add(yi.multiply(li)).mod(modulus);
        }

        return secret;
    }

    // Helper class to represent a point
    static class Point {
        int x;
        BigInteger y;

        public Point(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }
}