package com.antonioejemplo.gps_2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {


    private static final long TIEMPO_MIN = 10 * 1000; // 10 segundos
    private static final long DISTANCIA_MIN = 5; // 5 metros
    private static final String[] A = {"n/d", "preciso", "impreciso"};
    private static final String[] P = {"n/d", "bajo", "medio", "alto"};
    private static final String[] E = {"fuera de servicio",
            "temporalmente no disponible ", "disponible"};

    private static String LOGCAT;
    private LocationManager manejador;
    private String proveedor;
    private TextView salida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        salida = (TextView) findViewById(R.id.salida);
        manejador = (LocationManager) getSystemService(LOCATION_SERVICE);
        Log.v(LOGCAT, "Proveedores de localización: \n ");

        //LISTA TODOS LOS PROVEEDORES EXISTENTES EN EL TERMINAL
        muestraProveedores();

        /*CRITERIOS PARA ELEGIR EL PROVEEDOR:SIN COSTE, QUE MUESTRE ALTITUD, Y QUE TENGA PRECISIÓN FINA. CON ESTOS
        * SERÁ ELEGIDO AUTOMÁTICAMENTE EL PROVEEDOR A UTILIZAR POR EL PROPIO TERMINAL*/
        Criteria criterio = new Criteria();
        criterio.setCostAllowed(false);
        criterio.setAltitudeRequired(false);
        criterio.setAccuracy(Criteria.ACCURACY_FINE);
        proveedor = manejador.getBestProvider(criterio, true);
        Log.v(LOGCAT, "Mejor proveedor: " + proveedor + "\n");
        Log.v(LOGCAT, "Comenzamos con la última localización conocida:");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location localizacion = manejador.getLastKnownLocation(proveedor);

        muestraLocaliz(localizacion);

    }//fin Oncreate

    // Métodos del ciclo de vida de la actividad
    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        manejador.requestLocationUpdates(proveedor, TIEMPO_MIN, DISTANCIA_MIN,
                this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        manejador.removeUpdates(this);
    }

    // Métodos para mostrar información
    private void log(String cadena) {
        salida.append(cadena + "\n");
    }
    private void muestraLocaliz(Location localizacion) {
        if (localizacion == null)
            log("Localización desconocida\n");
        else
            log(localizacion.toString() + "\n");
    }
    private void muestraProveedores() {
        log("Proveedores de localización: \n ");
        List<String> proveedores = manejador.getAllProviders();
        for (String proveedor : proveedores) {
            muestraProveedor(proveedor);
        }
    }
    private void muestraProveedor(String proveedor) {
        LocationProvider info = manejador.getProvider(proveedor);
        log("LocationProvider[ " + "getName=" + info.getName()
                + ", isProviderEnabled="
                + manejador.isProviderEnabled(proveedor) + ", getAccuracy="
                + A[Math.max(0, info.getAccuracy())] + ", getPowerRequirement="
                + P[Math.max(0, info.getPowerRequirement())]
                + ", hasMonetaryCost=" + info.hasMonetaryCost()
                + ", requiresCell=" + info.requiresCell()
                + ", requiresNetwork=" + info.requiresNetwork()
                + ", requiresSatellite=" + info.requiresSatellite()
                + ", supportsAltitude=" + info.supportsAltitude()
                + ", supportsBearing=" + info.supportsBearing()
                + ", supportsSpeed=" + info.supportsSpeed() + " ]\n");
    }

    /**METODO DE LA INTERFAZ LOCATIONlISTENERSE LLAMA CUANDO CAMBIA LA LOCALIZACIÓN
     * Cuando cambia la localización
     */
    @Override
    public void onLocationChanged(Location location) {
        log("Nueva localización: ");
        muestraLocaliz(location);
    }

    /**METODO DE LA INTERFAZ LOCATIONlISTENER SE LLAMA CUANDO CAMBIA EL ESTADO DEL PROVEEDOR
     * Se le llama cuando cambia el estado de proveedor. Este método es llamado cuando
     * Un proveedor no es capaz de buscar una ubicación o si el proveedor tiene poco
     * Estén disponibles transcurrido un período de no disponibilidad.
     *
     * @param Proveedor el nombre del proveedor de ubicación asociada con este
     * Actualización.
     * @param Estado {@ link LocationProvider #} si el OUT_OF_SERVICE
     * Proveedor está fuera de servicio, y esto no se espera que cambie en el
     *                 futuro cercano; {@ Link LocationProvider #} si TEMPORARILY_UNAVAILABLE
     * El proveedor no está disponible temporalmente, pero se espera que esté disponible
     * En breve; y {@ link LocationProvider # DISPONIBLE} si el
     * Proveedor está disponible actualmente.
     * @param De extras opcionales un paquete que contendrá específica proveedor
     * Variables de estado.
     * <P />
     * <P> Un número de pares clave / valor común para el paquete de extras se enumeran
     * A continuación. Los proveedores que utilicen cualquiera de las teclas en esta lista que hay
     * Proporcionar el valor correspondiente, como se describe a continuación.
     * <P />
     * <Ul>
     * <Li> satélites - el número de satélites utilizados para derivar la solución
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        log("Cambia estado proveedor: " + proveedor + ", estado="
                + E[Math.max(0, status)] + ", extras=" + extras + "\n");
    }

    /**METODO DE LA INTERFAZ LOCATIONlISTENER SE LLAMA CUANDO EL PROVEEDOR LLAMADO ESTÁ HABILITADO
     * Called when the provider is enabled by the user.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderEnabled(String provider) {
        log("Proveedor habilitado: " + proveedor + "\n");
    }

    /**METODO DE LA INTERFAZ LOCATIONlISTENER SE LLAMA CUANDO EL PROVEEDOR LLAMADO ESTÁ DESHABILITADO
     *
     Se llama cuando el proveedor está deshabilitado por el usuario . Si requestLocationUpdates
     * Se llama en un proveedor ya desactivado, este método se llama
     * Inmediatamente.
     *
     * @param Proveedor el nombre del proveedor de ubicación asociada con este
     * Actualización.
     */
    @Override
    public void onProviderDisabled(String provider) {
        log("Proveedor deshabilitado: " + proveedor + "\n");
    }


}
