
package com.moviebooking.model;

import jakarta.persistence.Entity;

@Entity
public class Admin extends User {

    public Admin() {
        super();
    }

    public Admin(String name, String email, String password) {
        super(name, email, password);
    }

    @Override
    public void showRole() {
        System.out.println("Role: Admin");
    }

    public void manageMovies() {
        System.out.println("Admin is managing movies.");
    }
}


