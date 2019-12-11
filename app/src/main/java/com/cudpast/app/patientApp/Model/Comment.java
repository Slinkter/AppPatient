package com.cudpast.app.patientApp.Model;

import com.google.firebase.database.ServerValue;

public class Comment {

    private String comment_paciente, uid_paciente, img_pacient, name_pacient;
    private Object date_endAtention;

    public Comment() {

    }

    public Comment(String comment_paciente, String uid_paciente, String img_pacient, String uname) {
        this.date_endAtention = ServerValue.TIMESTAMP;
        this.uid_paciente = uid_paciente;
        this.img_pacient = img_pacient;
        this.name_pacient = uname;
        this.comment_paciente = comment_paciente;
    }

    public String getComment_paciente() {
        return comment_paciente;
    }

    public void setComment_paciente(String comment_paciente) {
        this.comment_paciente = comment_paciente;
    }

    public String getUid_paciente() {
        return uid_paciente;
    }

    public void setUid_paciente(String uid_paciente) {
        this.uid_paciente = uid_paciente;
    }

    public String getImg_pacient() {
        return img_pacient;
    }

    public void setImg_pacient(String img_pacient) {
        this.img_pacient = img_pacient;
    }

    public String getName_pacient() {
        return name_pacient;
    }

    public void setName_pacient(String name_pacient) {
        this.name_pacient = name_pacient;
    }

    public Object getDate_endAtention() {
        return date_endAtention;
    }

    public void setDate_endAtention(Object date_endAtention) {
        this.date_endAtention = date_endAtention;
    }

}
