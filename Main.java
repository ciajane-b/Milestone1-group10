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
            boolean menuRun = true;
            while (menuRun) {
                System.out.println("\n+----------------------------------------------------------------------+");
                System.out.println("|  MAIN MENU                                                           |");
                System.out.println("+----------------------------------------------------------------------+");
                System.out.println("|   1.  View Room Status                                               |");
                System.out.println("|   2.  Front Desk (Check-Out)                                         |");
                System.out.println("|   3.  Reservations (Add)                                             |");
                System.out.println("|   4.  Reservations (Modify)                                          |");
                System.out.println("|   5.  Reservations (Cancel)                                          |");
                System.out.println("|   6.  Reservations (Search)                                          |");
                System.out.println("|   7.  Officer Reports (SitRep)                                       |");
                System.out.println("|   8.  Generate Full Audit Report                                     |");
                System.out.println("|   9.  Generate Report by Date Range                                  |");
                System.out.println("|  10.  Generate Report by User                                        |");
                System.out.println("|  11.  Generate Reservation Summary                                   |");
                System.out.println("|  12.  Check-In (with Rank Assignment)                                |");
                System.out.println("|  13.  Record Weapons Clearance                                       |");
                System.out.println("|   0.  Logout                                                         |");
                System.out.println("+----------------------------------------------------------------------+");
                System.out.print("  Choice: ");
                int choice = validator.getValidInt(scanner);

                switch (choice) {
                    case 1:
                        System.out.println("\n  [ROOM STATUS]");
                        repo.printAllRooms();
                        break;

                    case 2:
                        System.out.print("  Unit ID: ");
                        String coUnitId = scanner.nextLine().trim();
                        System.out.print("  Member Name: ");
                        String coName = scanner.nextLine().trim();
                        System.out.print("  Member ID (e.g. SM-5521): ");
                        String coMemberId = scanner.nextLine().trim();
                        System.out.print("  Member Rank: ");
                        String coRank = scanner.nextLine().trim();
                        fd.checkOut(session, new ServiceMember(coName, coMemberId, coRank), coUnitId);
                        break;

                    case 3:
                        System.out.print("  Unit ID: ");
                        String addUnit = scanner.nextLine().trim();
                        System.out.print("  Member Name: ");
                        String addName = scanner.nextLine().trim();
                        System.out.print("  Member ID: ");
                        String addId = scanner.nextLine().trim();
                        System.out.print("  Member Rank: ");
                        String addRank = scanner.nextLine().trim();
                        res.addReservation(session, new ServiceMember(addName, addId, addRank), addUnit);
                        break;

                    case 4:
                        System.out.print("  Unit ID: ");
                        String modUnit = scanner.nextLine().trim();
                        System.out.print("  New Room Number: ");
                        String modRoom = scanner.nextLine().trim();
                        System.out.print("  Occupant Name: ");
                        String modOccupant = scanner.nextLine().trim();
                        res.modifyReservation(session, modUnit, modRoom, modOccupant);
                        break;

                    case 5:
                        System.out.print("  Unit ID: ");
                        String cancelUnit = scanner.nextLine().trim();
                        res.cancelReservation(session, cancelUnit);
                        break;

                    case 6:
                        System.out.print("  Unit ID: ");
                        String searchUnit = scanner.nextLine().trim();
                        res.searchReservation(session, searchUnit);
                        break;

                    case 7:
                        off.generateSitRep(session);
                        break;

                    case 8:
                        auditReport.generateFullAuditReport(session);
                        break;

                    case 9:
                        System.out.print("  Start Date (YYYY-MM-DD): ");
                        String startDate = scanner.nextLine().trim();
                        System.out.print("  End Date   (YYYY-MM-DD): ");
                        String endDate = scanner.nextLine().trim();
                        auditReport.generateReportByDateRange(session, startDate, endDate);
                        break;

                    case 10:
                        System.out.print("  Username: ");
                        String uname = scanner.nextLine().trim();
                        auditReport.generateReportByUser(session, uname);
                        break;

                    case 11:
                        auditReport.generateReservationSummary(session);
                        break;

                    case 12:
                        System.out.print("  Unit ID: ");
                        String ciUnit = scanner.nextLine().trim();
                        System.out.print("  Member Name: ");
                        String ciName = scanner.nextLine().trim();
                        System.out.print("  Member ID: ");
                        String ciId = scanner.nextLine().trim();
                        System.out.print("  Member Rank: ");
                        String ciRank = scanner.nextLine().trim();
                        res.checkIn(session, new ServiceMember(ciName, ciId, ciRank), ciUnit);
                        break;

                    case 13:
                        System.out.print("  Unit ID: ");
                        String wcUnit = scanner.nextLine().trim();
                        System.out.print("  Clearance Status (CLEARED / PENDING / DENIED): ");
                        String wcStatus = scanner.nextLine().trim().toUpperCase();
                        fd.recordWeaponsClearance(session, wcUnit, wcStatus);
                        break;

                    case 0:
                        System.out.println("\n  Logging out " + session.getUsername() + "...");
                        menuRun = false;
                        break;

                    default:
                        System.out.println("  Invalid choice. Please enter a number from the menu.");
                }
            }
        }

        scanner.close();
        System.out.println("\n  System exited. Goodbye.");
    }

    private static void printBox(String title, String[] lines) {
        System.out.println("\n+----------------------------------------------------------------------+");
        System.out.println("|  " + padRight(title, 68) + "|");
        System.out.println("+----------------------------------------------------------------------+");
        for (String line : lines) {
            System.out.println("|  " + padRight(line, 68) + "|");
        }
        System.out.println("+----------------------------------------------------------------------+");
    }

    private static String padRight(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }
}
