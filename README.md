# Notes Pro

Aplicación móvil profesional de notas para Android, inspirada en la simplicidad de ColorNote pero
con una interfaz moderna, organización avanzada y funciones de productividad completas. Construida
con **Expo (React Native + TypeScript)** y una base de datos **SQLite** local, por lo que funciona
completamente sin conexión a Internet.

## Funcionalidades

- **Notas y tareas**: notas de texto con formato básico (negrita, cursiva, viñetas), listas de
  tareas y notas con checklist, notas rápidas.
- **Organización**: categorías (predeterminadas y personalizadas), etiquetas, favoritos, notas
  fijadas, archivo y papelera.
- **Búsqueda instantánea** por título, contenido, etiquetas y categoría.
- **Recordatorios** con fecha, hora y repetición diaria, semanal o mensual mediante notificaciones
  locales.
- **Seguridad**: bloqueo de la app con PIN, desbloqueo biométrico (huella/rostro) y protección de
  notas individuales (el contenido protegido nunca se muestra en notificaciones).
- **Respaldo**: copia de seguridad manual y restauración local, exportación e importación de notas
  en formato `.json` mediante el selector de archivos y el panel de compartir del sistema.
- **Interfaz**: tema claro, oscuro y automático, tamaño de texto ajustable, vista de tarjetas o
  lista, gestos (deslizar para archivar/eliminar, mantener presionado para selección múltiple),
  menú lateral de navegación.

## Requisitos

- Node.js 18 o superior
- La app [Expo Go](https://expo.dev/go) instalada en un teléfono Android (forma más rápida de
  probarla), o Android Studio con un emulador configurado.

## Instalación

```bash
npm install
```

## Ejecutar en Android

```bash
npm run android
```

Esto inicia el servidor de desarrollo de Expo. Escanea el código QR con la app **Expo Go** en tu
teléfono Android, o presiona `a` en la terminal para abrirlo en un emulador conectado.

## Estructura del proyecto

```
App.tsx                  Punto de entrada: providers, tema, navegación, bloqueo de la app
src/
  components/            Componentes reutilizables de UI (tarjetas, FAB, chips, modales, etc.)
  constants/              Paletas de colores de notas y categorías por defecto
  db/                     Capa de acceso a SQLite (notas, categorías, respaldo)
  hooks/                  Hooks de filtrado, creación y apertura de notas
  navigation/             Drawer, stack raíz y tipos de navegación
  screens/                Pantallas: Inicio, Notas, Tareas, Favoritos, Categorías,
                          Archivadas, Papelera, Configuración, Editor de notas, Bloqueo
  security/               Contexto de desbloqueo de notas protegidas
  store/                  Estado global (Zustand): notas, ajustes, seguridad
  theme/                  Tema claro/oscuro y tipografía
  types/                  Tipos TypeScript del dominio
  utils/                  Fechas, IDs, notificaciones, formato de texto enriquecido, seguridad
```

## Notas técnicas

- Los datos se guardan localmente con `expo-sqlite`; la app funciona 100% sin conexión.
- Los recordatorios usan `expo-notifications` (notificaciones locales programadas); los mensuales
  se reprograman automáticamente al abrir la app una vez cumplida su fecha, ya que la plataforma no
  ofrece un disparador nativo mensual.
- El PIN se guarda con hash SHA-256 en `expo-secure-store`; la biometría usa
  `expo-local-authentication`.
- El formato de texto (negrita/cursiva/viñetas) usa una sintaxis markdown-lite ligera, sin
  dependencias nativas adicionales de edición enriquecida.
