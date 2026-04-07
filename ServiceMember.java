public class ServiceMember {
    private String name;
    private String id;
    private String rank;

    public ServiceMember(String name, String id, String rank) {
        this.name = name;
        this.id = id;
        this.rank = rank;
    }

    public String getName() { return name; }
    public String getId()   { return id;   }
    public String getRank() { return rank; }
}
