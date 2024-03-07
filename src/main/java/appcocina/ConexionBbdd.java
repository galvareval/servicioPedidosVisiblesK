/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package appcocina;

/**
 *
 * @author gianfranco
 */
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileInputStream;
import java.io.IOException;
/**
 * Clase para efectuar la conexióna  firebase
 * @author gianfranco
 */
public class ConexionBbdd {
    private FirebaseDatabase firebaseDatabase;
    /**
     * Crear el objeto conexión
     * @throws IOException 
     */
    public ConexionBbdd() throws IOException {
        initializeRealtimeDatabase();
    }

    /**
     * Inicializar la conxión con el archivo de configuración y la url de la bbdd
     * @throws IOException 
     */
    private void initializeRealtimeDatabase() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("firebaseAuth.json");//Fichero de configuración al proyecto firestore
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);

        firebaseDatabase = FirebaseDatabase.getInstance("https://cocinaapp-7da53-default-rtdb.europe-west1.firebasedatabase.app/");//URL de la BBDD
    }

    public FirebaseDatabase getFirebaseDatabase() {
        System.out.println("Coexión establcida");
        return firebaseDatabase;
    }
    /**
     * Cerrar la conexión
     * @throws Exception 
     */
    public void close() throws Exception {
       
    }
}
