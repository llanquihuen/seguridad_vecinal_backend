package cl.seguridad.vecinal.modelo;

public enum EstadoAlerta {
    ACTIVA,      // Alerta vigente
    EN_PROCESO,  // Siendo atendida
    ATENDIDA,    // Ya resuelta
    FALSA        // Falsa alarma
}