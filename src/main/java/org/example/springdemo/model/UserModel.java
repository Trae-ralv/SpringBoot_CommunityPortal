package org.example.springdemo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer user_id;

    @Column(name = "f_name")
    private String f_name;

    @Column(name = "l_name")
    private String l_name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_no")
    private String phone_no;

    @Column(name = "password")
    private String password;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "user_type")
    private Integer userType;

    // Constructors
    public UserModel() {}

    // Getters and Setters
    public Integer getUser_id() { return user_id; }
    public void setUser_id(Integer user_id) { this.user_id = user_id; }

    public String getF_name() { return f_name; }
    public void setF_name(String f_name) { this.f_name = f_name; }

    public String getL_name() { return l_name; }
    public void setL_name(String l_name) { this.l_name = l_name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone_no() { return phone_no; }
    public void setPhone_no(String phone_no) { this.phone_no = phone_no; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getUser_type() {
        return userType;
    }
    public void setUser_type(Integer userType) { this.userType = this.userType; }
}