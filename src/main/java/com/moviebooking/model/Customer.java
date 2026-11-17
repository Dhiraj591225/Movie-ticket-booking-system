
package com.moviebooking.model;

import jakarta.persistence.Entity;

@Entity
public class Customer extends User {

    public Customer() {
        super();
    }

    public Customer(String name, String email, String password) {
        super(name, email, password);
    }

    @Override
    public void showRole() {
        System.out.println("Role: Customer");
    }

    public void bookTicket() {
        System.out.println("Customer is booking a ticket.");
    }
}


