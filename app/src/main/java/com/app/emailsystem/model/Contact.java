// Contact.java
package com.app.emailsystem.model;

public class Contact {
    private int id;
    private String email;

    public Contact() {}

    public Contact(String email) {
        this.email = email;
    }

    public Contact(int id, String email) {
        this.id = id;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
