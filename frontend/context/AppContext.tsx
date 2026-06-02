import React, { createContext, useContext, useMemo, useState, useCallback, useEffect } from "react";
import { AuthService } from "../services/api";
import NetInfo from "@react-native-community/netinfo";

export type UserRole = "admin" | "user";

export interface User {
  id: string;
  name: string;
  email: string;
  role: UserRole;
}

export interface Product {
  id: string;
  sku: string;
  name: string;
  category: string;
  quantity: number;
  minStock: number;
  location: string;
  lastUpdated: string;
  imageUrl?: string;
}

export interface Movement {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  type: "entrada" | "salida";
  quantity: number;
  userName: string;
  timestamp: string;
  source: "online" | "offline";
  status: "aplicado" | "pendiente" | "rechazado";
}

export interface PendingSync {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  type: "entrada" | "salida";
  quantity: number;
  capturedBy: string;
  capturedAt: string;
  deviceId: string;
  note?: string;
}

// Mock data
const initialProducts: Product[] = [
  { id: "p1", sku: "SKU-00123", name: "Caja de Tornillos M8", category: "Ferretería", quantity: 142, minStock: 50, location: "A-01-03", lastUpdated: "Hace 12 min" },
  { id: "p2", sku: "SKU-00421", name: "Rollo de Cable 2.5mm", category: "Eléctrico", quantity: 24, minStock: 30, location: "B-02-01", lastUpdated: "Hace 1 h" },
  { id: "p3", sku: "SKU-00987", name: "Pintura Blanca 20L", category: "Pinturas", quantity: 8, minStock: 10, location: "C-04-02", lastUpdated: "Hace 3 h" },
  { id: "p4", sku: "SKU-01234", name: "Guantes Industriales", category: "EPP", quantity: 210, minStock: 40, location: "D-01-05", lastUpdated: "Ayer" },
  { id: "p5", sku: "SKU-02210", name: "Casco de Seguridad", category: "EPP", quantity: 67, minStock: 20, location: "D-01-06", lastUpdated: "Ayer" },
  { id: "p6", sku: "SKU-03099", name: "Taladro Inalámbrico 18V", category: "Herramientas", quantity: 12, minStock: 5, location: "E-02-04", lastUpdated: "Hace 2 d" },
  { id: "p7", sku: "SKU-04500", name: "Cinta Métrica 5m", category: "Herramientas", quantity: 95, minStock: 25, location: "E-02-01", lastUpdated: "Hace 2 d" },
  { id: "p8", sku: "SKU-05611", name: "Aceite Lubricante 1L", category: "Químicos", quantity: 3, minStock: 15, location: "F-03-02", lastUpdated: "Hace 4 d" },
];

const initialMovements: Movement[] = [
  { id: "m1", productId: "p1", productName: "Caja de Tornillos M8", sku: "SKU-00123", type: "entrada", quantity: 50, userName: "F. Aranda", timestamp: "Hoy, 10:32", source: "online", status: "aplicado" },
  { id: "m2", productId: "p4", productName: "Guantes Industriales", sku: "SKU-01234", type: "salida", quantity: 12, userName: "A. Rico", timestamp: "Hoy, 09:15", source: "online", status: "aplicado" },
  { id: "m3", productId: "p3", productName: "Pintura Blanca 20L", sku: "SKU-00987", type: "salida", quantity: 2, userName: "L. Pérez", timestamp: "Ayer, 17:40", source: "offline", status: "aplicado" },
  { id: "m4", productId: "p2", productName: "Rollo de Cable 2.5mm", sku: "SKU-00421", type: "entrada", quantity: 30, userName: "F. Aranda", timestamp: "Ayer, 14:02", source: "online", status: "aplicado" },
  { id: "m5", productId: "p6", productName: "Taladro Inalámbrico 18V", sku: "SKU-03099", type: "salida", quantity: 1, userName: "A. Rico", timestamp: "Ayer, 11:20", source: "offline", status: "rechazado" },
];

const initialPending: PendingSync[] = [
  { id: "s1", productId: "p1", productName: "Caja de Tornillos M8", sku: "SKU-00123", type: "salida", quantity: 8, capturedBy: "L. Pérez", capturedAt: "Hoy, 11:02", deviceId: "DEV-07", note: "Escaneo offline en zona fría" },
  { id: "s2", productId: "p5", productName: "Casco de Seguridad", sku: "SKU-02210", type: "entrada", quantity: 20, capturedBy: "L. Pérez", capturedAt: "Hoy, 11:05", deviceId: "DEV-07" },
  { id: "s3", productId: "p8", productName: "Aceite Lubricante 1L", sku: "SKU-05611", type: "salida", quantity: 4, capturedBy: "M. Gómez", capturedAt: "Hoy, 10:48", deviceId: "DEV-03", note: "Conflicto potencial con inventario bajo" },
];

interface AppContextValue {
  user: User | null;
  isOnline: boolean;
  products: Product[];
  movements: Movement[];
  pending: PendingSync[];
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  toggleOnline: () => void;
  addScan: (productId: string, type: "entrada" | "salida", quantity: number) => void;
  approvePending: (id: string) => void;
  rejectPending: (id: string) => void;
}

const AppContext = createContext<AppContextValue | null>(null);

export function AppProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isOnline, setIsOnline] = useState(true);
  const [products, setProducts] = useState<Product[]>(initialProducts);
  const [movements, setMovements] = useState<Movement[]>(initialMovements);
  const [pending, setPending] = useState<PendingSync[]>(initialPending);

  useEffect(() => {
    const unsubscribe = NetInfo.addEventListener((state) => {
      setIsOnline(!!state.isConnected);
    });

    return () => unsubscribe();
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const response = await AuthService.login(email, password);
    setUser({
      id: response.id.toString(),
      name: response.nombre || response.usuario || "Usuario",
      email: response.email || `${response.usuario}@bodegaos.com`,
      role: response.role === "admin" ? "admin" : "user",
    });
  }, []);

  const logout = useCallback(() => setUser(null), []);
  const toggleOnline = useCallback(() => setIsOnline((v) => !v), []);

  const addScan = useCallback(
    (productId: string, type: "entrada" | "salida", quantity: number) => {
      const product = products.find((p) => p.id === productId);
      if (!product) return;
      const nowStr = "Hoy, " + new Date().toLocaleTimeString("es-MX", { hour: "2-digit", minute: "2-digit" });

      if (isOnline) {
        setProducts((prev) =>
          prev.map((p) =>
            p.id === productId
              ? { ...p, quantity: type === "entrada" ? p.quantity + quantity : Math.max(0, p.quantity - quantity), lastUpdated: "Ahora" }
              : p
          )
        );
        setMovements((prev) => [
          {
            id: "m" + Date.now(),
            productId,
            productName: product.name,
            sku: product.sku,
            type,
            quantity,
            userName: user?.name ?? "Empleado",
            timestamp: nowStr,
            source: "online",
            status: "aplicado",
          },
          ...prev,
        ]);
      } else {
        setPending((prev) => [
          {
            id: "s" + Date.now(),
            productId,
            productName: product.name,
            sku: product.sku,
            type,
            quantity,
            capturedBy: user?.name ?? "Empleado",
            capturedAt: nowStr,
            deviceId: "DEV-07",
          },
          ...prev,
        ]);
      }
    },
    [products, isOnline, user]
  );

  const approvePending = useCallback((id: string) => {
    setPending((prev) => {
      const item = prev.find((p) => p.id === id);
      if (item) {
        setProducts((pp) =>
          pp.map((p) =>
            p.id === item.productId
              ? {
                  ...p,
                  quantity: item.type === "entrada" ? p.quantity + item.quantity : Math.max(0, p.quantity - item.quantity),
                  lastUpdated: "Ahora",
                }
              : p
          )
        );
        setMovements((mm) => [
          {
            id: "m" + Date.now(),
            productId: item.productId,
            productName: item.productName,
            sku: item.sku,
            type: item.type,
            quantity: item.quantity,
            userName: item.capturedBy,
            timestamp: "Ahora",
            source: "offline",
            status: "aplicado",
          },
          ...mm,
        ]);
      }
      return prev.filter((p) => p.id !== id);
    });
  }, []);

  const rejectPending = useCallback((id: string) => {
    setPending((prev) => {
      const item = prev.find((p) => p.id === id);
      if (item) {
        setMovements((mm) => [
          {
            id: "m" + Date.now(),
            productId: item.productId,
            productName: item.productName,
            sku: item.sku,
            type: item.type,
            quantity: item.quantity,
            userName: item.capturedBy,
            timestamp: "Ahora",
            source: "offline",
            status: "rechazado",
          },
          ...mm,
        ]);
      }
      return prev.filter((p) => p.id !== id);
    });
  }, []);

  const value = useMemo<AppContextValue>(
    () => ({ user, isOnline, products, movements, pending, login, logout, toggleOnline, addScan, approvePending, rejectPending }),
    [user, isOnline, products, movements, pending, login, logout, toggleOnline, addScan, approvePending, rejectPending]
  );

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useApp() {
  const ctx = useContext(AppContext);
  if (!ctx) throw new Error("useApp must be used within AppProvider");
  return ctx;
}
