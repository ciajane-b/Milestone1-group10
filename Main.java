import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ReportRepository repo = new ReportRepository();
        AfterActionReview aar = new AfterActionReview(repo);
        FrontDeskService fd = new FrontDeskService(aar, repo);
        ReservationService res = new ReservationService(aar, repo);
        OfficerService off = new OfficerService(repo);
        AuditReportService auditReport = new AuditReportService(repo);
        InputValidator validator = new InputValidator();

        boolean run = true;
        while (run) {
            printBox("MILITARY OUTPOST LODGING MANAGEMENT SYSTEM",
                    new String[]{
                            "LOGIN REQUIRED",
                            "",
                            "Enter your credentials",
                            "(type 'quit' to exit)"
                    });

            System.out.print("|  |    User: ");
            String u = scanner.nextLine().trim();
            if (u.equalsIgnoreCase("quit")) break;

            System.out.print("|  |    Pass: ");
            String p = scanner.nextLine();

            UserSession session = repo.authenticate(u, p);

            if (session == null) {
                System.out.println("\n+----------------------------------------------------------------------+");
                System.out.println("|  ACCESS DENIED: Invalid credentials. Please try again.              |");
                System.out.println("+----------------------------------------------------------------------+");
                continue;
            }

            System.out.println("\n  Login successful. Welcome, " + session.getRank() + " " + session.getUsername() + "!");
        }
    }
}
