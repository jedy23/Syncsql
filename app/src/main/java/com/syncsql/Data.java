package com.syncsql;

public class Data {
    private String id, name, surname, gender, age, stat, tmpid;

    Data(){
        id = "";
        name = "";
        surname = "";
        gender = "";
        stat = "";
        tmpid = "";
        age = "";
    }

    public String getTmpid() {
        return tmpid;
    }

    public void setTmpid(String tmpid) {
        this.tmpid = tmpid;
    }

    public String getId() {
        return id;
    }

    public void setvar(Integer type, String value){
        switch (type){
            case 0:
                setId(value);
                break;
            case 1:
                setName(value);
                break;
            case 2:
                setSurname(value);
                break;
            case 3:
                setGender(value);
                break;
            case 4:
                setAge(value);
                break;
            case 5:
                setStat(value);
                break;
            case 6:
                setTmpid(value);
                break;
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }
}
