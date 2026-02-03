package up.edu.isgc;

import java.util.Scanner;

public class Calculator {

    public static void main(String[] args) {

        // Scanner to read user input
        Scanner sc = new Scanner(System.in);

        int option;

        System.out.println("=== Coordinate Converter ===");
        System.out.println("1. Polar to Cartesian");
        System.out.println("2. Cartesian to Polar");
        System.out.print("Choose an option: ");
        option = sc.nextInt();

        if (option == 1) {

            // Polar to Cartesian conversion
            double r, theta;

            System.out.print("Enter radius (r): ");
            r = sc.nextDouble();

            System.out.print("Enter angle (theta in degrees): ");
            theta = sc.nextDouble();

            // Convert degrees to radians
            double thetaRad = Math.toRadians(theta);

            // Apply formulas
            double x = r * Math.cos(thetaRad);
            double y = r * Math.sin(thetaRad);

            // Output result
            System.out.println("\nCartesian Coordinates:");
            System.out.println("x = " + x);
            System.out.println("y = " + y);

        } else if (option == 2) {

            // Cartesian to Polar conversion
            double x, y;

            System.out.print("Enter x: ");
            x = sc.nextDouble();

            System.out.print("Enter y: ");
            y = sc.nextDouble();

            // Apply formulas
            double r = Math.sqrt(x * x + y * y);
            double thetaRad = Math.atan2(y, x);

            // Convert radians to degrees
            double thetaDeg = Math.toDegrees(thetaRad);

            // Output result
            System.out.println("\nPolar Coordinates:");
            System.out.println("r = " + r);
            System.out.println("theta = " + thetaDeg + " degrees");

        } else {
            System.out.println("Invalid option.");
        }

        sc.close();
    }
}
