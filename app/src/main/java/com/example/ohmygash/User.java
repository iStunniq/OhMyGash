package com.example.ohmygash;

public class User {

    private String email, pass, Name, placeName, placeAdd, accType, keyID;

    public User() {
    }

    public User(String email, String pass) {
        this.email = email;
        this.pass = pass;
    }

    public User(String email, String pass, String Name,String accType) {
        this.email = email;
        this.pass = pass;
        this.Name = Name;
        this.accType = accType;
    }

    public User(String email, String pass, String Name, String placeName, String placeAdd,String accType){
        this.email = email;
        this.pass = pass;
        this.Name = Name;
        this.placeName = placeName;
        this.placeAdd = placeAdd;
        this.accType = accType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceAdd() {
        return placeAdd;
    }

    public void setPlaceAdd(String placeAdd) {
        this.placeAdd = placeAdd;
    }

    public String getAccType() {
        return accType;
    }

    public void setAccType(String accType) {
        this.accType = accType;
    }

    public String getKeyID() {
        return keyID;
    }

    public void setKeyID(String keyID) {
        this.keyID = keyID;
    }
}
