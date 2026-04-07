import java.util.Scanner;

public class InputValidator {

    public int getValidInt(Scanner scanner) {
        while (true) {
            try {
                int input = scanner.nextInt();
                scanner.nextLine();
                return input;
            } catch (Exception e) {
                System.out.println("|  |    Invalid input. Please enter a number.                         |  |");
                scanner.nextLine();
                System.out.print("|  |    Choice: ");
            }
        }
    }

    public String getValidString(Scanner scanner) {
        while (true) {
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("|  |    Input cannot be empty. Please try again.                        |  |");
            System.out.print("|  |    Enter: ");
        }
    }
}
