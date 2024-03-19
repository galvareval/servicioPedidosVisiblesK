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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Notification;
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
    private final DatabaseReference databaseReferencePedidos;
    private final DatabaseReference databaseReferenceUsuarios;
    private ConexionBbdd conexion;

    public ServicioPedidosVisibles() {
        try {
            conexion = new ConexionBbdd();
        } catch (IOException ex) {
            Logger.getLogger(ServicioPedidosVisibles.class.getName()).log(Level.SEVERE, null, ex);
        }
        FirebaseDatabase database = conexion.getFirebaseDatabase();
        databaseReferencePedidos = database.getReference("pedidos");
        databaseReferenceUsuarios = database.getReference("usuarios");

    }

    @Override
    public void run() {
        // Obtener la fecha y hora actual
        long currentTime = System.currentTimeMillis();
        System.out.println("Servicio en marcha se ejecutara cada hora");
        // Agregar un ValueEventListener a la referencia de la base de datos
        databaseReferencePedidos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Iterar sobre  pedidos
                for (DataSnapshot pedidoSnapshot : dataSnapshot.getChildren()) {
                    Pedido pedido = pedidoSnapshot.getValue(Pedido.class);
                    String idUsuario = pedido.getUsuario();
                    String idPedido = pedidoSnapshot.getKey();
                    String titulo = "Titulo de la notificación";
                    String cuerpo = "mensaje de la notifiacion";
                    //System.out.println("Pedido encontrado" + pedidoSnapshot.getKey());
                    if (pedido != null && pedido.getEditable() == true) {
                        // Obtener la fecha del pedido en String
                        String fechaPedidoStr = pedido.getFecha_pedido();
                        //System.out.println("pedido editable == true encontrado: " + pedido.toString());
                        // Convertir la fecha del pedido a un tipo de dato apropiado (p. ej., Date)
                        Date fechaPedido = convertirStringAFecha(fechaPedidoStr);

                        //Prueba en 1 min; si ha pasado 1 min desde la fecha del pedido set editable == false
                        if (fechaPedido != null) {
                            // Calcular la diferencia de tiempo en minutos
                            System.out.println("Fecha del pedido " + fechaPedido);
                            long diferenciaMinutos = (currentTime - fechaPedido.getTime()) / (60 * 1000);
                            
                            System.out.println("Dif munutos" + diferenciaMinutos);
                            switch ((int) diferenciaMinutos) {
                                //Enviar notificacion cuando hallan pasado 30 mins desde el pedido
                                case 30 :
                                case 31 :
                                    titulo = "Aviso sobre pedido";
                                    cuerpo = "Aún dispones de 30 minutos para editar tu pedido: " + idPedido.substring(3, 7);
                                    enviarNotificacion(idUsuario, idPedido, titulo, cuerpo);
                                    break;
                                //Enviar notificacion cuando hallan pasado 60 mins desde el pedido
                                case 60:
                                case 61:    
                                    titulo = "Aviso sobre pedido";
                                    cuerpo = "Le informamos de que ya no puede editar el siguiente pedido: " + idPedido.substring(3, 7);
                                    enviarNotificacion(idUsuario, idPedido, titulo, cuerpo);
                                    //actualizarEstado(pedido, pedidoSnapshot);Esto lo hacen desde WEB con scrip de python
                                    break;
                                default:
                                    
                                    break;
                            }
                        }
                    } else {
                        //El pedido no es editable, enviar notificacion se puede recoger ya
                        if (pedido.getEstado().equals("recoger")) {
                            titulo = "Recoger pedido ";
                            cuerpo = "Por favor pasa a recoger tu pedido: " + idPedido.substring(3, 7);
                            enviarNotificacion(idUsuario, idPedido, titulo, cuerpo);
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
        timer.scheduleAtFixedRate(servicio, 0, 60000); // 3600000 milisegundos = 1 hora para probar 60000= que se ejecute cada minuto; en prod poner cada 45 
    }

    /**
     * Actulizar a false el campo editable del peddido y enviar correo
     *
     * @param pedido Pedido que se va a actualizar
     * @param pedidoSnapshot Datasnapshot del pedidot
     */
    private void actualizarEstado(Pedido pedido, DataSnapshot pedidoSnapshot) {
        pedido.setEditable(false);
        String idPedido = pedidoSnapshot.getKey();
        // Actualizarlo en bbdd
        databaseReferencePedidos.child(pedidoSnapshot.getKey()).setValue(pedido.toMap(), null);
        System.out.println("El pedido: " + idPedido + " ya no es editable");
        enviarCorreoElectronico(pedido, idPedido);
    }

    /**
     * Método para convertir un string de fecha en un objeto de fecha para hacer
     * las operaciones con las fechas
     *
     * @param fechaStr
     * @return
     */
    private Date convertirStringAFecha(String fechaStr) {
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
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
            message.setSubject("Pedido recibido: " + idPedido);
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

    /**
     * Enviar notificacion del usuario del pedido
     *
     * @param idUsuario Id del usuario
     * @param idPedido Id del pedido
     * @param titulo Titulo de la noticación
     * @param cuerpo Cuerpo de la notifiación
     */
    private void enviarNotificacion(String idUsuario, String idPedido, String titulo, String cuerpo) {
        databaseReferenceUsuarios.child(idUsuario).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Obj Pedido encontrado a partir del Idpedido
                Usuarios usuario = dataSnapshot.getValue(Usuarios.class);
                if (usuario != null) {
                    String tokenNoti = usuario.getTokenNoti();
                    //Construir y enviar la noti
                    Notification notification = Notification.builder()
                            .setTitle(titulo)
                            .setBody(cuerpo)
                            .build();
                    com.google.firebase.messaging.Message message = com.google.firebase.messaging.Message.builder()
                            .setToken(tokenNoti)
                            .setNotification(notification)
                            .build();
                    try {
                        String response = FirebaseMessaging.getInstance().send(message);
                        System.out.println("Mensaje enviado : " + response);
                    } catch (Exception e) {
                        System.err.println("Error al enviar el mensaje: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Manejar el error en caso de que ocurra
                System.err.println("Error: " + databaseError.getMessage());
            }
        });

    }

}
