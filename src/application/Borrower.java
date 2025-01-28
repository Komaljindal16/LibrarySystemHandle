package application;
public class Borrower {
    private int id;
    private String name;
    private String email;
    private String phone;

    public Borrower(int id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Getters and setters for all fields
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    @Override
    public String toString() {
        return name;  // Display the name of the borrower in the ComboBox
    }
}
