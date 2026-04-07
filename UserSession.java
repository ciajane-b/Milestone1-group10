public class UserSession {
    private String username;
    private String rank;

    public UserSession(String username, String rank) {
        this.username = username;
        this.rank = rank;
    }

    public String getUsername() {
        return username;
    }

    public String getRank() {
        return rank;
    }
}
