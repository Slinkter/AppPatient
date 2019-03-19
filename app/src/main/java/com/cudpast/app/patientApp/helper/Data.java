package com.cudpast.app.patientApp.helper;

public class Data {
    public String title;
    public String descripcion;
    public String extradata;
    public String uidPaciente;

    public Data() {
    }


    public Data(String title, String descripcion) {
        this.title = title;
        this.descripcion = descripcion;
    }

    public Data(String title, String descripcion, String extradata) {
        this.title = title;
        this.descripcion = descripcion;
        this.extradata = extradata;
    }

    public Data(String title, String descripcion, String extradata, String uidPaciente) {
        this.title = title;
        this.descripcion = descripcion;
        this.extradata = extradata;
        this.uidPaciente = uidPaciente;
    }

    public String getUidPaciente() {
        return uidPaciente;
    }

    public void setUidPaciente(String uidPaciente) {
        this.uidPaciente = uidPaciente;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }


    public String getExtradata() {
        return extradata;
    }

    public void setExtradata(String extradata) {
        this.extradata = extradata;
    }
}
