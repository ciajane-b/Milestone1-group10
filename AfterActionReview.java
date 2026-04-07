public class AfterActionReview {
    private ReportRepository repo;

    public AfterActionReview(ReportRepository repo) {
        this.repo = repo;
    }

    public void log(UserSession session, String message) {
        if (session != null) {
            repo.saveLog(session.getUsername(), message);
        }
    }
}
