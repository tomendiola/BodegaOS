# BodegaOS — Sistema de Gestión de Inventario (Prototipo Visual)

## Resumen
Prototipo visual React Native (Expo + expo-router) del Sistema de Gestión de Inventario con Lectura de QR/Barras descrito en la propuesta técnica. App empresarial moderna, tema claro azul/gris, 100% en español, datos simulados (sin backend).

## Funcionalidades incluidas
- **Autenticación con selector de rol** (Empleado / Administrador) — controla qué pantallas son visibles.
- **Dashboard** con métricas (unidades totales, SKUs, movimientos, pendientes, bajo stock) y acciones rápidas.
- **Scanner QR/Barras simulado** — vista de cámara con frame, láser animado, y modal de confirmación con +/- cantidad y botones Entrada/Salida.
- **Modo offline simulado** — botón "Simular offline" en el banner superior; los escaneos en modo offline no alteran el inventario, se envían a la cola de aprobación del admin.
- **Lista de Inventario** (solo admin) — búsqueda por nombre/SKU, chips de filtros (Todos, Bajo stock, categorías), tarjetas con stock y estado.
- **Detalle de Producto** (solo admin) — hero con SKU/nombre/categoría, stock con barra visual, ubicación, entradas/salidas totales, historial completo del producto.
- **Historial de Movimientos** (solo admin) — filtros por tipo (Entradas/Salidas/Offline/Rechazados), tags de origen online/offline y estado.
- **Sync / Aprobaciones** (solo admin) — lista de operaciones offline pendientes con botones Aprobar (aplica al inventario) y Rechazar (descarta). Soporta "Aprobar todo".

## Roles
- **Empleado**: Inicio, Escanear.
- **Administrador**: Inicio, Escanear, Inventario, Historial, Sync.

## Archivos clave
- `/app/frontend/app/_layout.tsx` — Layout raíz con AppProvider.
- `/app/frontend/app/index.tsx` — Redirección según sesión.
- `/app/frontend/app/login.tsx` — Pantalla de login con selección de rol.
- `/app/frontend/app/(tabs)/_layout.tsx` — Tabs con visibilidad por rol.
- `/app/frontend/app/(tabs)/dashboard.tsx`
- `/app/frontend/app/(tabs)/scanner.tsx`
- `/app/frontend/app/(tabs)/inventory.tsx`
- `/app/frontend/app/(tabs)/history.tsx`
- `/app/frontend/app/(tabs)/sync.tsx`
- `/app/frontend/app/product/[id].tsx`
- `/app/frontend/components/OfflineBanner.tsx`
- `/app/frontend/context/AppContext.tsx` — Estado global mock (usuario, productos, movimientos, cola de sync).

## Credenciales de demo
Cualquier correo/contraseña funciona (prototipo). El rol se elige con los botones Empleado / Administrador antes de pulsar Entrar.

## Notas
- Prototipo **solo visual**, sin backend ni MongoDB; el estado vive en memoria vía React Context.
- Al cerrar sesión se reinicia el estado? No — los datos mock permanecen, solo se elimina la sesión.
- No se incluye integración real con cámara/ML Kit; el botón del obturador "simula" un escaneo eligiendo un producto aleatorio del catálogo.
