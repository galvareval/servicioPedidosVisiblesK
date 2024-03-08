/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package appcocina;

/**
 * Este servicio se ejecuta cada hora para establecer los pedidos a no
 * editables, false en el campo editable del pedido. Para aquellos pedidos que
 * hallan superado 1 hora desde que se hicieron
 *
 * @author gianfranco
 */
import com.google.firebase.database.*;
import java.io.IOException;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import javax.mail.Session;
import javax.mail.Transport;

/**
 * Clase que implemnta el timer para que se ejecute cada hora, al tener el
 * listener ondatachange tambien se actuliza cada vez que se accede a la tabla
 * pedidos. Pero en caso de que no se acceda se ejecuta la logica pasda 1 hora.
 *
 * @author gjalvarez
 */
public class ServicioPedidosVisibles extends TimerTask {

    //conxión a firebase
    private final DatabaseReference databaseReference;
    private ConexionBbdd conexion;

    public ServicioPedidosVisibles() {
        try {
            conexion = new ConexionBbdd();
        } catch (IOException ex) {
            Logger.getLogger(ServicioPedidosVisibles.class.getName()).log(Level.SEVERE, null, ex);
        }
        FirebaseDatabase database = conexion.getFirebaseDatabase();
        databaseReference = database.getReference("pedidos");

    }

    @Override
    public void run() {
        // Obtener la fecha y hora actual
        long currentTime = System.currentTimeMillis();
        System.out.println("Servicio en marcha se ejecutara cada hora");
        // Agregar un ValueEventListener a la referencia de la base de datos
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Iterar sobre  pedidos
                for (DataSnapshot pedidoSnapshot : dataSnapshot.getChildren()) {
                    Pedido pedido = pedidoSnapshot.getValue(Pedido.class);
                    //System.out.println("Pedido encontrado" + pedidoSnapshot.getKey());
                    if (pedido != null && pedido.getEditable() == true) {
                        // Obtener la fecha del pedido en String
                        String fechaPedidoStr = pedido.getFecha_pedido();
                        //System.out.println("pedido editable == true encontrado: " + pedido.toString());
                        // Convertir la fecha del pedido a un tipo de dato apropiado (p. ej., Date)
                        Date fechaPedido = convertirStringAFecha(fechaPedidoStr);
                        //Para cambiar el estado cunado pase de una hora
                        /* if (fechaPedido != null) {
                            // Calcular la diferencia de tiempo en horas
                            long diferenciaHoras = (currentTime - fechaPedido.getTime()) / (60 * 60 * 1000);
                            //System.out.println("la diferencia de horas es: " + diferenciaHoras);

                            // Si la diferencia es mayor o igual que 1 hora, actualizar el campo "editable" del pedido a false
                            if (diferenciaHoras >= 1) {
                                pedido.setEditable(false);
                                String idPedido = pedidoSnapshot.getKey();
                                // Actualizarlo en bbdd
                                databaseReference.child(pedidoSnapshot.getKey()).setValue(pedido.toMap(),null);
                                System.out.println("El pedido ya no es editable");
                                enviarCorreoElectronico(pedido,idPedido);

                            }
                        }*/
                        //Prueba en 1 min; si ha pasado 1 min desde la fecha del pedido set editable == false
                        if (fechaPedido != null) {
                            // Calcular la diferencia de tiempo en minutos
                            long diferenciaMinutos = (currentTime - fechaPedido.getTime()) / (60 * 1000);
                            // Si la diferencia es mayor o igual que 2 minutos, actualizar el campo "editable" del pedido a false
                            if (diferenciaMinutos >= 1) {
                                pedido.setEditable(false);
                                String idPedido = pedidoSnapshot.getKey();
                                // Actualizarlo en bbdd
                                databaseReference.child(pedidoSnapshot.getKey()).setValue(pedido.toMap(), null);
                                System.out.println("El pedido: " + idPedido + " ya no es editable");
                                enviarCorreoElectronico(pedido, idPedido);
                            }
                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Error al cargar datos");
            }
        });

    }

    public static void main(String[] args) {

        // Para arrancar servicio
        ServicioPedidosVisibles servicio = new ServicioPedidosVisibles();

        //Temporizador para ejecutar el servicio cada hora
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(servicio, 0, 60000); // 3600000 milisegundos = 1 hora para probar 60000= que se ejecute cada minuto
    }

    /**
     * Método para convertir un string de fecha en un objeto de fecha para hacer
     * las operaciones con las fechas
     *
     * @param fechaStr
     * @return
     */
    private Date convertirStringAFecha(String fechaStr) {
        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date fechaFormat;
        try {
            fechaFormat = formatoFecha.parse(fechaStr);
        } catch (Exception e) {
            e.printStackTrace();
            fechaFormat = null;
        }
        return fechaFormat;
    }

    /**
     * Metodo para enviar el correo
     *
     * @param pedido
     */
    private void enviarCorreoElectronico(Pedido pedido, String idPedido) {
        // Credenciales 
        final String correoRemitente = "appcocinaestella@gmail.com";
        final String contraseña = "bbcadricalhnvlne";//Constraseña de aplicacion
        // Configurar las propiedades del servidor de correo electrónico
        Properties props = System.getProperties();
        props.put("mail.smtp.user", correoRemitente);
        props.put("mail.smtp.clave", contraseña);    
        //Config gmail
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        // Crear  sesión de correo 
        Session session = Session.getDefaultInstance(props);

        try {
            // Mensaje de correo electrónico
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(correoRemitente));
            message.setSubject("Pedido recibido: " + idPedido );
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(correoRemitente));
            message.setText("Puedes comezar a gestionar el pedido/n " + pedido.toString());
            Transport transport = session.getTransport("smtp");
            transport.connect("smtp.gmail.com", correoRemitente, contraseña);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            // Enviar el mensaje de correo electrónico

            System.out.println("Correo electrónico enviado con éxito.");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Error al enviar el correo electrónico." + e.getMessage());
        }
    }

}
