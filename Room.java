public class Room {
    private String roomNumber;
    private String status;

    public Room(String roomNumber, String status) {
        this.roomNumber = roomNumber;
        this.status = status;
    }

    public String getRoomNumber() { return roomNumber; }
    public String getStatus()     { return status;     }
    public void setStatus(String status) { this.status = status; }
}
