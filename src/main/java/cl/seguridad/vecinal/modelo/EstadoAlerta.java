// src/main/java/cl/seguridad/vecinal/modelo/EstadoAlerta.java
package cl.seguridad.vecinal.modelo;

public enum EstadoAlerta {
    ACTIVA,      // Alerta recién creada
    EN_PROCESO,  // Autoridades notificadas
    ATENDIDA,    // Alguien está atendiendo
    RESUELTA,    // Situación resuelta
    FALSA_ALARMA // Marcada como falsa alarma
}