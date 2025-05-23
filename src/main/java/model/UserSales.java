// src/model/UserSales.java
package model;

public class UserSales {
    private final String username;
    private final double totalSold;

    public UserSales(String username, double totalSold) {
        this.username  = username;
        this.totalSold = totalSold;
    }

    public String getUsername() {
        return username;
    }
    public double getTotalSold() {
        return totalSold;
    }
}
