package com.cudpast.app.patientApp.Model;

public class PacientProfile {

    private String uid;
    private String firstname;
    private String lastname;
    private String phone;
    private String address;
    private String dni;
    private String mail;
    private String password;
    private String dateborn;

    public PacientProfile() {

    }

    public PacientProfile(String uid, String firstname, String lastname, String phone, String address, String dni, String mail, String password, String dateborn) {
        this.uid = uid;
        this.firstname = firstname;
        this.lastname = lastname;
        this.phone = phone;
        this.address = address;
        this.dni = dni;
        this.mail = mail;
        this.password = password;
        this.dateborn = dateborn;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDateborn() {
        return dateborn;
    }

    public void setDateborn(String dateborn) {
        this.dateborn = dateborn;
    }
}
