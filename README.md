# Mis Gastos

Aplicación nativa para Android (Kotlin + Jetpack Compose) para llevar control detallado de gastos
personales y familiares: registro rápido de movimientos, dashboard con KPIs, análisis por día/semana/
mes/año, presupuestos con alertas, historial filtrable y respaldo/exportación de la información.

## Stack técnico

- **UI**: Jetpack Compose + Material 3 (tema claro/oscuro), navegación con Navigation Compose.
- **Datos**: Room (SQLite) con Kotlin Flow para actualizaciones reactivas; toda la información se
  guarda localmente y persiste aunque se cierre la app.
- **Arquitectura**: MVVM simple — `ViewModel` + `StateFlow` por pantalla, repositorios como única
  fuente de verdad, contenedor de dependencias manual (`AppContainer`, sin frameworks de DI).
- **Notificaciones**: `NotificationManager` para alertas de presupuesto/gasto diario y `WorkManager`
  para resúmenes periódicos (semanal/mensual).
- **Exportación**: CSV del historial completo y reporte de texto (periodo, KPIs, categorías,
  establecimientos, detalle de movimientos), compartibles vía `FileProvider`. Respaldo/restauración
  completos de la base de datos en un archivo JSON.

## Estructura del proyecto

```
app/src/main/java/com/misgastos/app/
├── data/            # Room (entidades/DAOs/DB), repositorios, exportación, respaldo, notificaciones
├── domain/          # Modelos y utilidades de dominio (rangos de fecha, cálculo de KPIs, formato)
├── di/              # Contenedor de dependencias manual
└── ui/
    ├── dashboard/    # Inicio: KPIs, presupuesto, gráficas
    ├── addexpense/   # Registro rápido de gastos
    ├── analysis/     # Análisis por Día / Semana / Mes / Año / Comidas
    ├── history/       # Historial con búsqueda, filtros, orden, edición y borrado
    ├── settings/      # Categorías/establecimientos, formas de pago, presupuestos, alertas, respaldo
    ├── components/    # KPI cards, gráficas (barras, dona, líneas), filtros de fecha, etc.
    └── theme/         # Colores, tipografía y tema claro/oscuro
```

## Datos iniciales (seed)

Al primer arranque la app crea automáticamente:

- **Comidas** 🍔: KFC, Carl's Jr, Asadero El Primo, Tacos Alex, Salads, Pizza, Delicias, Grill House,
  Teo, Barbacoa, Don Pancho, Otros.
- **Casa / Servicios** 🏠: Tienda, Clase de monta, Agua, Luz, Total Play, Gas, Aurrera, Gasolina, Otros.
- Formas de pago: Efectivo, Tarjeta de débito, Tarjeta de crédito, Transferencia, Otro.

Todo es editable: se pueden agregar, renombrar o eliminar categorías, establecimientos y formas de
pago desde Configuración (no se permite eliminar algo que ya tiene gastos registrados, para no perder
historial).

## Compilar y ejecutar

Requiere Android Studio (Koala o superior) con JDK 17 y un dispositivo/emulador con Android 8.0
(API 26) o superior.

```
./gradlew assembleDebug
```

o abrir la carpeta del proyecto directamente en Android Studio y ejecutar `app` sobre un emulador o
dispositivo físico.

> Nota: este proyecto se generó en un entorno sin acceso al SDK de Android ni a los repositorios de
> Maven de Google, por lo que el build no se pudo compilar ni probar en un emulador desde aquí. El
> código sigue las APIs estables de Compose/Room/Navigation de 2024; se recomienda una primera
> sincronización de Gradle en Android Studio para resolver cualquier ajuste menor de versiones.
