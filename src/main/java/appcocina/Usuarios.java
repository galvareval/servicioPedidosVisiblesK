/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package appcocina;

/**
 * Clae entidad de los usuario de appcicona
 * @author gianfranco
 */
public class Usuarios {

    private String nombre;
    private String apellidos;
    private String email;
    private String departamento;
    private String telefono;
    private String tokenNoti;

    public Usuarios() {
    }

    public Usuarios(String nombre, String apellidos, String email, String departamento, String telefono) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.departamento = departamento;
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getTokenNoti() {
        return tokenNoti;
    }

    public void setTokenNoti(String tokenNoti) {
        this.tokenNoti = tokenNoti;
    }

    @Override
    public String toString() {
        return "Usuarios{" + "nombre=" + nombre + ", apellidos=" + apellidos + ", email=" + email + ", departamento=" + departamento + ", telefono=" + telefono + ", tokenNoti=" + tokenNoti + '}';
    }
    
}


