# 🗄️ Guía de Integración PostgreSQL - BodegaOS

Esta guía te ayudará a conectar tu proyecto BodegaOS con PostgreSQL.

## 📋 Requisitos Previos

1. **PostgreSQL instalado** en tu máquina
   - [Descargar PostgreSQL](https://www.postgresql.org/download/)
   - Versión recomendada: 13 o superior

2. **Python 3.8+** en tu máquina
3. **Node.js y npm** para el frontend

---

## 🚀 Paso 1: Configurar PostgreSQL

### En Windows:

1. **Abre pgAdmin** (se instala con PostgreSQL)
   - URL: `http://localhost:5050`
   - Usuario: `postgres` (por defecto)

2. **Crea una nueva base de datos**
   - Click derecho en "Databases" → "Create" → "Database"
   - Nombre: `bodegaos`
   - Owner: `postgres`
   - Click "Save"

3. **Anota los datos de conexión:**
   ```
   Host: localhost
   Puerto: 5432
   Usuario: postgres
   Contraseña: (la que estableciste)
   Base de datos: bodegaos
   ```

---

## 🔧 Paso 2: Configurar el Backend

### 1. Instala las dependencias
```bash
cd backend
pip install -r requirements.txt
```

### 2. Configura las variables de entorno

Copia `.env.example` a `.env`:
```bash
cp .env.example .env
```

Edita el archivo `.env` con tus datos de PostgreSQL:
```env
DATABASE_URL=postgresql://postgres:tu_contraseña@localhost:5432/bodegaos
HOST=0.0.0.0
PORT=8000
DEBUG=False
SECRET_KEY=your-secret-key-here
```

### 3. Inicializa la base de datos con datos de ejemplo
```bash
python init_db.py
```

Deberías ver:
```
✅ Database initialized successfully with sample data!
   - Created 5 sample products
   - Created 1 sample user (admin)
   - Created 1 status check record
```

### 4. Inicia el servidor
```bash
python -m uvicorn server:app --reload --host 0.0.0.0 --port 8000
```

El servidor estará disponible en: **http://localhost:8000**

Endpoints útiles:
- `GET /api/health` - Verificar estado
- `GET /api/products` - Obtener todos los productos
- `GET /api/` - Información de bienvenida
- Documentación interactiva: **http://localhost:8000/docs**

---

## 📱 Paso 3: Configurar el Frontend

### 1. Configura la URL del API

En tu archivo `.env` del frontend (crea uno si no existe):
```env
EXPO_PUBLIC_API_URL=http://localhost:8000/api
```

O en `frontend/app.json`:
```json
{
  "expo": {
    "extra": {
      "apiUrl": "http://localhost:8000/api"
    }
  }
}
```

### 2. El servicio API ya está incluido

El archivo `frontend/services/api.ts` ya contiene:
- `ProductService` - Gestión de productos
- `InventoryService` - Movimientos de inventario
- `StatusService` - Verificación de estado

### 3. Usa el servicio en tus componentes

```typescript
import { ProductService } from '../../services/api';

// En tu componente
const [products, setProducts] = useState([]);

useEffect(() => {
  ProductService.getAll()
    .then(setProducts)
    .catch(console.error);
}, []);
```

---

## 🧪 Paso 4: Prueba la Integración

### Test 1: Verifica la conexión a BD
```bash
# Desde la carpeta backend
python -c "from database import engine; print('✅ Conectado a PostgreSQL')"
```

### Test 2: Accede a la documentación de la API
Abre en tu navegador: **http://localhost:8000/docs**

### Test 3: Prueba un endpoint
```bash
curl http://localhost:8000/api/products
```

Deberías obtener los 5 productos de ejemplo.

---

## 📊 Estructura de la Base de Datos

### Tablas Creadas:

#### `products`
- `id` (UUID)
- `name` (texto)
- `sku` (texto único)
- `category` (texto)
- `quantity` (número)
- `minStock` (número)
- `location` (texto)
- `price` (decimal)
- `description` (texto)
- `lastUpdated` (texto)
- `created_at` (timestamp)
- `updated_at` (timestamp)

#### `users`
- `id` (UUID)
- `email` (texto único)
- `username` (texto único)
- `password_hash` (texto)
- `role` (texto: admin, user, viewer)
- `is_active` (booleano)
- `created_at` (timestamp)
- `updated_at` (timestamp)

#### `status_checks`
- `id` (UUID)
- `client_name` (texto)
- `status` (texto)
- `created_at` (timestamp)

#### `inventory_movements`
- `id` (UUID)
- `product_id` (UUID)
- `quantity_change` (número)
- `movement_type` (texto: entrada, salida, ajuste)
- `reason` (texto)
- `user_id` (UUID)
- `created_at` (timestamp)

---

## 🛠️ Solución de Problemas

### Error: "connection refused"
- Verifica que PostgreSQL está corriendo
- En Windows: Services → busca "PostgreSQL" → asegúrate que está "Running"

### Error: "database does not exist"
- Crea la base de datos `bodegaos` en pgAdmin

### Error: "password authentication failed"
- Verifica la contraseña en tu `DATABASE_URL`
- En `.env`: `postgresql://usuario:contraseña@localhost:5432/bodegaos`

### El frontend no se conecta
- Verifica que el backend está corriendo en puerto 8000
- Asegúrate que `EXPO_PUBLIC_API_URL` es correcto
- En emulador de Android, usa: `http://10.0.2.2:8000/api`
- En emulador de iOS, usa: `http://localhost:8000/api`

---

## 📝 Próximos Pasos

1. **Actualiza AppContext** para usar la API en lugar de datos locales
2. **Implementa autenticación** con JWT
3. **Agrega validaciones** en el backend
4. **Configura CORS** adecuadamente para producción
5. **Implementa paginación** en endpoints GET

---

## 📚 Recursos Útiles

- [FastAPI Docs](https://fastapi.tiangolo.com/)
- [SQLAlchemy Docs](https://docs.sqlalchemy.org/)
- [PostgreSQL Docs](https://www.postgresql.org/docs/)
- [Expo API Docs](https://docs.expo.dev/)

---

## ✅ Checklist de Implementación

- [ ] PostgreSQL instalado y corriendo
- [ ] Base de datos `bodegaos` creada
- [ ] Backend configurado con `.env`
- [ ] Dependencies instaladas (`pip install -r requirements.txt`)
- [ ] Base de datos inicializada (`python init_db.py`)
- [ ] Servidor backend corriendo (`python -m uvicorn server:app --reload`)
- [ ] Frontend con `EXPO_PUBLIC_API_URL` configurado
- [ ] Pruebas de endpoints completadas
- [ ] Componentes actualizados para usar ProductService
- [ ] Commit y push a Git

---

¡Listo! Tu proyecto BodegaOS está completamente integrado con PostgreSQL. 🎉
