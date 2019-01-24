package com.cudpast.app.patientApp.Model;

public class Doctor {

    private String DNI;
    private String firstname;
    private String lastname;
    private String numphone;
    private String especialidad;
    private String image;

    public Doctor() {

    }

    public Doctor(String DNI, String firstname, String lastname, String numphone, String especialidad, String image) {
        this.DNI = DNI;
        this.firstname = firstname;
        this.lastname = lastname;
        this.numphone = numphone;
        this.especialidad = especialidad;
        this.image = image;
    }

    public String getDNI() {
        return DNI;
    }

    public void setDNI(String DNI) {
        this.DNI = DNI;
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

    public String getNumphone() {
        return numphone;
    }

    public void setNumphone(String numphone) {
        this.numphone = numphone;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
