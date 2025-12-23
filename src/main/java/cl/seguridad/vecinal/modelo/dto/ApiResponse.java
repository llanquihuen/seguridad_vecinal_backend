package cl.seguridad.vecinal.modelo.dto;

/**
 * Marker interface for all API responses.
 * This allows type-safe return types in controllers.
 */
public sealed interface ApiResponse permits AuthResponse, ErrorResponse {}

