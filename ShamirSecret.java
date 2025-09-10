import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class ShamirSecret {

  // Function to compute Lagrange Interpolation at x = 0
  public static BigInteger lagrangeInterpolation(BigInteger[] xs, BigInteger[] ys) {
    int k = xs.length;
    BigInteger result = BigInteger.ZERO;

    for (int i = 0; i < k; i++) {
      BigInteger term = ys[i];
      for (int j = 0; j < k; j++) {
        if (i != j) {
          // (0 - xj) / (xi - xj)
          BigInteger numerator = xs[j].negate();
          BigInteger denominator = xs[i].subtract(xs[j]);
          term = term.multiply(numerator).divide(denominator);
        }
      }
      result = result.add(term);
    }

    // Normalize result to always be positive
    if (result.signum() < 0) {
      result = result.negate();
    }
    return result;
  }

  // Helper to parse JSON manually without libraries
  public static Map<String, String> parseJson(String json) {
    Map<String, String> map = new LinkedHashMap<>();
    json = json.trim().replaceAll("[{}\"]", ""); // strip braces and quotes
    String[] parts = json.split(",");
    for (String part : parts) {
      String[] kv = part.split(":");
      if (kv.length == 2) {
        map.put(kv[0].trim(), kv[1].trim());
      }
    }
    return map;
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.out.println("Usage: java ShamirSecret <input.json>");
      return;
    }

    // Read whole JSON file
    StringBuilder sb = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line.trim());
      }
    }
    String json = sb.toString();

    // Extract keys:n and keys:k
    int nIndex = json.indexOf("\"n\"");
    int kIndex = json.indexOf("\"k\"");
    int n = Integer.parseInt(json.substring(json.indexOf(":", nIndex) + 1, json.indexOf(",", nIndex)).trim());
    int k = Integer.parseInt(json.substring(json.indexOf(":", kIndex) + 1, json.indexOf("}", kIndex)).trim());

    BigInteger[] xs = new BigInteger[k];
    BigInteger[] ys = new BigInteger[k];

    int count = 0;
    for (int i = 1; i <= n && count < k; i++) {
      String key = "\"" + i + "\":";
      int idx = json.indexOf(key);
      if (idx == -1)
        continue;

      // Extract base
      int baseIdx = json.indexOf("\"base\"", idx);
      int baseStart = json.indexOf(":", baseIdx) + 1;
      int baseEnd = json.indexOf(",", baseIdx);
      int base = Integer.parseInt(json.substring(baseStart, baseEnd).replaceAll("[^0-9]", "").trim());

      // Extract value
      int valIdx = json.indexOf("\"value\"", idx);
      int valStart = json.indexOf(":", valIdx) + 1;
      int valEnd = json.indexOf("}", valIdx);
      String valueStr = json.substring(valStart, valEnd).replaceAll("[^0-9a-zA-Z]", "").trim();

      // Convert to BigInteger
      BigInteger value = new BigInteger(valueStr, base);

      xs[count] = BigInteger.valueOf(i);
      ys[count] = value;
      count++;
    }

    BigInteger secret = lagrangeInterpolation(xs, ys);

    System.out.println("Reconstructed Secret: " + secret);
  }
}