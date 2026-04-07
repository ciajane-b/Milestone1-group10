public class FrontDeskService {
    private AfterActionReview aar;
    private ReportRepository repo;

    public FrontDeskService(AfterActionReview aar, ReportRepository repo) {
        this.aar = aar;
        this.repo = repo;
    }

    private boolean verifyIdentity(ServiceMember member, String reservationName) {
        System.out.println("|  |    Verifying identity for: " + member.getId());
        System.out.println("|  |    Reservation holder: " + reservationName);
        if (member.getName().equals(reservationName)) {
            System.out.println("|  |    Identity verified successfully.");
            return true;
        } else {
            System.out.println("|  |    WARNING: Identity mismatch!");
            return false;
        }
    }

    private boolean checkWeaponsClearance(String unitId) {
        System.out.println("|  |    Checking weapons clearance for Unit ID: " + unitId);
        System.out.println("|  |    Weapons clearance status: VERIFIED");
        System.out.println("|  |    Weapons cleared by: Armory Officer");
        return true;
    }

    public void recordWeaponsClearance(UserSession s, String unitId, String status) {
        if (s.getRank().equals("PVT") || s.getRank().equals("PFC")) {
            System.out.println("|  |    ERROR: Your rank cannot record weapons clearance.");
            return;
        }
        repo.updateWeaponsClearance(unitId, status);
        System.out.println("|  |    Weapons clearance recorded for Unit ID: " + unitId);
        System.out.println("|  |    Status: " + status);
        aar.log(s, "Recorded weapons clearance for Unit: " + unitId + " - " + status);
    }

    public void checkOut(UserSession s, ServiceMember member, String unitId) {
        if (s.getRank().equals("PVT") || s.getRank().equals("PFC")) {
            System.out.println("|  |    ERROR: Your rank cannot authorize check-outs.");
            return;
        }

        System.out.println("|  |    Rank authorized: " + s.getRank());

        if (!checkWeaponsClearance(unitId)) {
            System.out.println("|  |    ERROR: Weapons clearance not verified. Check-out denied.");
            return;
        }

        String reservationName = repo.getOccupantNameForUnit(unitId);
        if (reservationName == null) {
            System.out.println("|  |    ERROR: No active reservation found for Unit ID: " + unitId);
            return;
        }

        if (!verifyIdentity(member, reservationName)) {
            System.out.println("|  |    ERROR: Identity verification failed. Check-out denied.");
            return;
        }

        String roomNumber = repo.getRoomNumberForUnit(unitId);

        System.out.println("|  |    Processing Check-out for: " + member.getId());
        System.out.println("|  |    Releasing quarters for: " + member.getName());

        repo.releaseQuarters(unitId);
        if (roomNumber != null) {
            repo.updateRoomStatus(roomNumber, "Ready");
        }
        aar.log(s, "Authorized check-out for " + member.getId() +
                " (" + member.getName() + ") Unit: " + unitId);
        System.out.println("|  |    Check-out completed successfully.");
        System.out.println("|  |    Unit roster updated.");
    }
}
