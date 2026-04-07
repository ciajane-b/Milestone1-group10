public class ReservationService {
    private AfterActionReview aar;
    private ReportRepository repo;

    public ReservationService(AfterActionReview aar, ReportRepository repo) {
        this.aar = aar;
        this.repo = repo;
    }

    private String assignRoomByRank(String rank) {
        System.out.println("|  |    Assigning quarters based on rank: " + rank);
        switch (rank) {
            case "PVT": case "PFC": return "Barracks Bunk 4";
            case "SPC": case "CPL": return "Barracks Bunk 2";
            case "SGT": case "SSG": return "200";
            case "LT":  case "CPT": return "300";
            default:                return "100";
        }
    }

    private boolean canManageReservations(UserSession s) {
        String r = s.getRank();
        return !r.equals("PVT") && !r.equals("PFC");
    }

    public void addReservation(UserSession s, ServiceMember member, String unitId) {
        if (!canManageReservations(s)) {
            System.out.println("|  |    ACCESS DENIED: Your rank cannot create reservations.");
            return;
        }
        String assignedRoom = assignRoomByRank(member.getRank());
        System.out.println("|  |    Room " + assignedRoom + " assigned to " + member.getName());
        System.out.println("|  |    Unit ID: " + unitId);
        System.out.println("|  |    Rank: " + member.getRank());
        repo.saveReservation(unitId, member, assignedRoom);
        repo.updateRoomStatus(assignedRoom, "Occupied");
        aar.log(s, "Created reservation for " + member.getName() +
                " in " + assignedRoom + " (Unit: " + unitId + ", Rank: " + member.getRank() + ")");
        System.out.println("|  |    Reservation created successfully.");
    }

    public void modifyReservation(UserSession s, String unitId, String newRoom, String occupantName) {
        if (!canManageReservations(s)) {
            System.out.println("|  |    ACCESS DENIED: Your rank cannot modify reservations.");
            return;
        }
        repo.modifyReservation(unitId, newRoom, occupantName);
        System.out.println("|  |    Reservation modified: " + occupantName +
                " (Unit: " + unitId + ") moved to Room " + newRoom);
        aar.log(s, "Modified reservation for " + occupantName +
                " (Unit: " + unitId + ") to " + newRoom);
        System.out.println("|  |    Modification completed successfully.");
    }

    public void cancelReservation(UserSession s, String unitId) {
        if (!canManageReservations(s)) {
            System.out.println("|  |    ACCESS DENIED: Your rank cannot cancel reservations.");
            return;
        }
        String occupant = repo.getOccupantNameForUnit(unitId);
        if (occupant == null) {
            System.out.println("|  |    No active reservation found for Unit ID: " + unitId);
            return;
        }
        repo.cancelReservation(unitId);
        System.out.println("|  |    Reservation cancelled: " + occupant + " (Unit: " + unitId + ")");
        aar.log(s, "Cancelled reservation for " + occupant + " (Unit: " + unitId + ")");
        System.out.println("|  |    Cancellation completed successfully.");
    }

    public void searchReservation(UserSession s, String unitId) {
        System.out.println("|  |    Searching for reservations with Unit ID: " + unitId);
        repo.searchReservationByUnitId(unitId);
        aar.log(s, "Searched reservations for Unit ID: " + unitId);
    }

    public void checkIn(UserSession s, ServiceMember member, String unitId) {
        if (!canManageReservations(s)) {
            System.out.println("|  |    ACCESS DENIED: Your rank cannot process check-ins.");
            return;
        }
        String assignedRoom = assignRoomByRank(member.getRank());
        System.out.println("|  |    Processing check-in for: " + member.getName());
        System.out.println("|  |    Unit ID: " + unitId);
        System.out.println("|  |    Rank: " + member.getRank());
        System.out.println("|  |    Assigned quarters: Room " + assignedRoom);
        System.out.println("|  |    Weapons clearance status: REQUIRED");
        System.out.println("|  |    Please complete weapons clearance before finalizing.");
        repo.saveReservation(unitId, member, assignedRoom);
        repo.updateRoomStatus(assignedRoom, "Occupied");
        aar.log(s, "Processed check-in for " + member.getName() +
                " (" + member.getRank() + ") Unit: " + unitId);
    }
}
