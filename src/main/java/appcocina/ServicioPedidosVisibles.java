/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package appcocina;

/**
 *Este servicio se ejecuta cada hora para establecer los pedidos a no editables, false en el campo editable del pedido.
 * Para aquellos pedidos que hallan superado 1 hora desde que se hicieron
 * @author gianfranco
 */
import com.google.firebase.database.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase que implemnta el timer para que se ejecute cada hora, al tener el listener ondatachange tambien se actuliza cada vez que se accede a la tabla pedidos.
 * Pero en caso de que no se acceda se ejecuta la logica pasda 1 hora.
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
        System.out.println("Servicio en marcha");
        // Agregar un ValueEventListener a la referencia de la base de datos
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Iterar sobre  pedidos
                for (DataSnapshot pedidoSnapshot : dataSnapshot.getChildren()) {
                    Pedido pedido = pedidoSnapshot.getValue(Pedido.class);
                    if (pedido != null && pedido.getEditable() == true) {
                        // Obtener la fecha del pedido en String
                        String fechaPedidoStr = pedido.getFecha_pedido();
                        System.out.println("pedido editable: " + pedido.toString());
                        // Convertir la fecha del pedido a un tipo de dato apropiado (p. ej., Date)
                        Date fechaPedido = convertirStringAFecha(fechaPedidoStr);
                        
                        if (fechaPedido != null) {
                            // Calcular la diferencia de tiempo en horas
                            long diferenciaHoras = (currentTime - fechaPedido.getTime()) / (60 * 60 * 1000);
                            //System.out.println("la diferencia de horas es: " + diferenciaHoras);

                            // Si la diferencia es mayor o igual que 1 hora, actualizar el campo "editable" del pedido a false
                            if (diferenciaHoras >= 1) {
                                pedido.setEditable(false);
                                // Actualizarlo en bbdd
                                databaseReference.child(pedidoSnapshot.getKey()).setValue(pedido.toMap(),null);
                                System.out.println("El pedido ya no es editable");

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
        timer.scheduleAtFixedRate(servicio, 0, 60000 ); // 3600000 milisegundos = 1 hora
    }

    /**
     * Método para convertir un string de fecha en un objeto de fecha para hacer las operaciones con las fechas
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

}
