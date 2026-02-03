package up.edu.isgc;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        // Create Scanner object to read user input
        Scanner sc = new Scanner(System.in);

        int width;
        int height;

        System.out.println("=== Aspect Ratio Program ===");

        // Ask user for screen width
        System.out.print("Enter screen width: ");
        width = sc.nextInt();

        // Ask user for screen height
        System.out.print("Enter screen height: ");
        height = sc.nextInt();

        // Validate input values
        if (width <= 0 || height <= 0) {
            System.out.println("Error: Numbers must be greater than 0");
            return;
        }

        // Calculate GCD (Greatest Common Divisor) using Euclidean algorithm
        int a = width;
        int b = height;

        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }

        int gcd = a;

        // Simplify aspect ratio using GCD
        int ratioWidth = width / gcd;
        int ratioHeight = height / gcd;

        // Calculate decimal aspect ratio
        double decimalRatio = (double) width / height;

        // Display results
        System.out.println("\nResult:");
        System.out.println("Resolution: " + width + " x " + height);
        System.out.println("Aspect Ratio: " + ratioWidth + ":" + ratioHeight);
        System.out.println("Decimal Ratio: " + decimalRatio);

        // Close scanner
        sc.close();
    }
}
