# Contrato HTTP — Gestión de turnos y atención al usuario

Origen de cada petición:

- **Contrato**: está en la sección 8 del documento del proyecto.
- **Propuesta**: no está en el contrato. Se deriva de los roles (sección 4), las reglas de negocio y las entidades, porque el documento permite sumar lo necesario para el CRUD. Las formas de entrada y respuesta marcadas como propuesta **no están definidas** en los requisitos.
- **Implementado**: ya existe en el código (los 4 GET y el POST de turnos).

Errores: los implementados devuelven `{"estado": 404, "mensaje": "..."}` para recurso inexistente y 400 para parámetros ausentes o inválidos.

## 1. Peticiones del contrato

### GET /api/servicios — Implementado
- **Propósito:** lista los servicios activos (orientación, documentos, soporte, trámites).
- **Envía:** nada.
- **Responde 200:**
```json
[
  {"id": 1, "codigo": "ORI", "nombre": "Orientación", "descripcion": "Información general al público",
   "horaApertura": "08:00:00", "horaCierre": "17:00:00"}
]
```

### GET /api/turnos/{codigo} — Implementado
- **Propósito:** estado público y limitado de un turno. No expone datos del solicitante ni del agente. El módulo solo aparece si el turno está `LLAMADO` o `EN_ATENCION`.
- **Envía:** el código en la ruta, por ejemplo `ORI-261005-007`.
- **Responde 200:**
```json
{"codigo": "ORI-261005-007", "servicio": "Orientación", "estado": "LLAMADO",
 "modulo": "Módulo 1", "fechaEmision": "2026-10-05T08:00:00"}
```
- **Responde 404:** `{"estado": 404, "mensaje": "No existe el turno XXX"}`

### GET /api/filas?servicioId= — Implementado
- **Propósito:** fila de un servicio en la fecha operativa actual (la del servidor), con orden y tiempo de espera. Solo turnos `EN_ESPERA`, ordenados por prioridad y luego por emisión (RN-08).
- **Envía:** `servicioId` (obligatorio).
- **Responde 200:**
```json
{"servicioId": 1, "servicio": "Orientación", "fechaOperativa": "2026-10-05", "totalEnEspera": 2,
 "turnos": [
   {"orden": 1, "codigo": "ORI-261005-002", "prioridad": "PREFERENCIAL",
    "fechaEmision": "2026-10-05T08:10:00", "tiempoEsperaSegundos": 1200},
   {"orden": 2, "codigo": "ORI-261005-001", "prioridad": "NORMAL",
    "fechaEmision": "2026-10-05T08:00:00", "tiempoEsperaSegundos": 1800}
 ]}
```
- **Responde 404** si el servicio no existe y 400 si falta `servicioId`.

### GET /api/indicadores?fecha= — Implementado
- **Propósito:** cantidad atendida (turnos `FINALIZADO`) y tiempos promedio de la fecha operativa. Espera = emisión → llamado; atención = inicio → fin, calculados desde `historial_turnos` (RN-12). Si no hay datos, los promedios son `null`.
- **Envía:** `fecha` (obligatoria, formato `yyyy-MM-dd`).
- **Responde 200:**
```json
{"fecha": "2026-10-05", "cantidadAtendida": 1,
 "tiempoPromedioEsperaSegundos": 750.0, "tiempoPromedioAtencionSegundos": 1080.0}
```

### POST /api/turnos — Implementado
- **Propósito:** emite un turno aplicando servicio activo y horario (RN-02), duplicidad (RN-03), secuencia por servicio y fecha (RN-01) y registro en el historial (RN-09).
- **Envía:**
```json
{"servicioId": 1,
 "solicitante": {"tipoDocumento": "CC", "numeroDocumento": "1020304050", "nombreCompleto": "Ana Pérez",
                 "correo": "ana@correo.com", "telefono": "3001234567"},
 "prioridadId": 1}
```
  `correo`, `telefono` y `prioridadId` son opcionales. Sin `prioridadId` se usa la prioridad `NORMAL`.
- **Responde 201:**
```json
{"id": 7, "codigo": "ORI-261005-007", "servicio": "Orientación", "estado": "EN_ESPERA",
 "fechaOperativa": "2026-10-05", "consecutivo": 7, "fechaEmision": "2026-10-05T08:00:00"}
```
- **Errores:** 400 si falta `servicioId` o los datos obligatorios del solicitante; 404 si el servicio o la prioridad no existen; 409 si el servicio está inactivo o fuera de horario, si el solicitante ya tiene un turno activo en ese servicio, o si la prioridad requiere autorización.
- **Supuestos:** si el solicitante ya existe (mismo tipo y número de documento) se reutiliza sin actualizar sus datos. Las prioridades que requieren autorización se rechazan, porque el contrato no define cómo se autorizan. Los códigos 400, 404 y 409 son decisión del código, no del contrato.

### POST /api/modulos/{id}/siguiente-turno — Contrato, pendiente
- **Propósito:** selecciona y llama el siguiente turno elegible del módulo (RN-06, RN-07, RN-08, RN-09).
- **Envía (propuesta):** `{"agenteId": 2}`
- **Responde 200 (propuesta):**
```json
{"codigo": "ORI-261005-002", "estado": "LLAMADO", "modulo": "Módulo 1",
 "agente": "Agente Uno", "fechaLlamado": "2026-10-05T08:30:00"}
```
- **Falta definir:** cómo se identifica al agente (no hay autenticación) y qué se responde si no hay turno elegible.

### PUT /api/servicios/{id} — Contrato, pendiente
- **Propósito:** reemplaza la configuración completa de un servicio.
- **Envía (derivado de la tabla `servicios`):**
```json
{"codigo": "ORI", "nombre": "Orientación", "descripcion": "Información general",
 "activo": true, "horaApertura": "08:00:00", "horaCierre": "17:00:00"}
```
- **Responde 200:** el servicio actualizado, con la misma forma que `GET /api/servicios`.

### PATCH /api/turnos/{id}/estado — Contrato, pendiente
- **Propósito:** transición explícita y válida: iniciar atención, finalizar o marcar `NO_PRESENTE` (RN-05, RN-10).
- **Envía (propuesta):** `{"estado": "EN_ATENCION", "usuarioId": 2, "observacion": null}`
- **Responde 200 (propuesta):** `{"id": 7, "codigo": "ORI-261005-007", "estado": "EN_ATENCION"}`
- **Falta definir:** quién es el actor y el código de error ante una transición inválida.

### PATCH /api/modulos/{id}/estado — Contrato, pendiente
- **Propósito:** abre, pausa o cierra un módulo.
- **Envía (propuesta):** `{"estado": "ABIERTO", "agenteId": 2}` (el agente es obligatorio al abrir).
- **Responde 200 (propuesta):** `{"id": 1, "codigo": "M1", "estado": "ABIERTO", "agente": "Agente Uno"}`

### DELETE /api/turnos/{id} — Contrato, pendiente
- **Propósito:** cancela lógicamente un turno que aún no inició atención (RN-11). El turno queda `CANCELADO`, no se borra.
- **Envía:** nada.
- **Responde (propuesta):** 204 sin cuerpo.

## 2. Peticiones propuestas para el CRUD (no están en el contrato)

| Método y ruta | Rol | Propósito | Envía (según las entidades) |
|---|---|---|---|
| GET /api/modulos | Agente, coordinador | Lista módulos con estado y servicios habilitados | — |
| GET /api/prioridades | Coordinador, solicitante | Lista prioridades (hace falta para `prioridadId` al emitir) | — |
| POST /api/servicios | Coordinador | Crea un servicio (el PUT solo reemplaza uno existente) | `codigo, nombre, descripcion, activo, horaApertura, horaCierre` |
| POST /api/modulos | Coordinador | Crea un módulo | `codigo, nombre` |
| PUT /api/modulos/{id}/servicios | Coordinador | Define los servicios habilitados (RN-06) | `{"servicioIds": [1, 3]}` |
| POST /api/prioridades | Coordinador | Crea una prioridad | `nombre, descripcion, nivel, requiereAutorizacion, activa` |
| PUT /api/prioridades/{id} | Coordinador | Reemplaza una prioridad | igual que POST |
| GET /api/usuarios-internos | Administrador técnico | Lista usuarios internos | — |
| POST /api/usuarios-internos | Administrador técnico | Crea un usuario interno | `nombreCompleto, correo, rol, activo` |
| PUT /api/usuarios-internos/{id} | Administrador técnico | Reemplaza un usuario interno | igual que POST |

Las respuestas de estas peticiones no están definidas; lo razonable es devolver el recurso creado o actualizado con su `id`.

## 3. Lo que falta para cerrar el contrato

- **Autenticación:** no hay requisito ni código (la rama `feature/login` solo trae archivos compilados). Sin ella, las peticiones de agente, coordinador y administrador no saben quién actúa.
- **Códigos de error:** el documento no define cuáles devolver ante duplicado, horario o transición inválida.
- **Prioridad autorizada:** no se define cómo se solicita ni quién la autoriza.

## 4. DTOs por endpoint

Están en el paquete `dto` (el proyecto usa una sola carpeta, con sufijo `Request` o `Response`). Los campos salen de los JSON de este documento.

| Endpoint | Request | Response |
|---|---|---|
| GET /api/servicios | — | `ServicioResponse` |
| GET /api/turnos/{codigo} | — | `TurnoPublicoResponse` |
| GET /api/filas?servicioId= | — | `FilaResponse`, `FilaTurnoResponse` |
| GET /api/indicadores?fecha= | — | `IndicadoresResponse` |
| POST /api/turnos | `EmitirTurnoRequest`, `SolicitanteRequest` | `TurnoEmitidoResponse` |
| POST /api/modulos/{id}/siguiente-turno | `LlamarSiguienteTurnoRequest` | `TurnoLlamadoResponse` |
| PUT /api/servicios/{id} | `ActualizarServicioRequest` | `ServicioResponse` |
| PATCH /api/turnos/{id}/estado | `CambiarEstadoTurnoRequest` | `TurnoEstadoResponse` |
| PATCH /api/modulos/{id}/estado | `CambiarEstadoModuloRequest` | `ModuloEstadoResponse` |
| DELETE /api/turnos/{id} | — | — (204 sin cuerpo) |

Los errores comparten `ErrorResponse` (`estado`, `mensaje`).
