import React from "react";
import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from "react-native";
import { Feather } from "@expo/vector-icons";
import { router } from "expo-router";
import { useApp } from "../../context/AppContext";

const C = {
  primary: "#0F4C81", primaryLight: "#2A6BA6",
  white: "#FFFFFF", bg: "#F8FAFC", border: "#E2E8F0",
  gray500: "#64748B", gray800: "#1E293B",
  success: "#16A34A", successBg: "#DCFCE7",
  warn: "#F59E0B", warnBg: "#FEF3C7",
  error: "#DC2626", errorBg: "#FEE2E2",
};

export default function Dashboard() {
  const { user, products, movements, pending, logout, isOnline } = useApp();
  const isAdmin = user?.role === "admin";

  const totalItems = products.reduce((a, p) => a + p.quantity, 0);
  const skuCount = products.length;
  const lowStock = products.filter((p) => p.quantity < p.minStock).length;
  const todayMoves = movements.length;

  const doLogout = () => {
    logout();
    router.replace("/login");
  };

  return (
    <ScrollView style={styles.screen} contentContainerStyle={styles.content} testID="dashboard-scroll">
      <View style={styles.headerRow}>
        <View>
          <Text style={styles.hi}>Hola, {user?.name ? user.name.split(" ")[0] : "Usuario"}</Text>
          <Text style={styles.roleTxt}>
            {isAdmin ? "Administrador de bodega" : "Empleado operativo"}
          </Text>
        </View>
        <TouchableOpacity testID="logout-btn" style={styles.logout} onPress={doLogout}>
          <Feather name="log-out" size={16} color={C.gray800} />
        </TouchableOpacity>
      </View>

      <View style={[styles.heroCard, { backgroundColor: C.primary }]}>
        <View style={styles.heroTop}>
          <View style={styles.heroBadge}>
            <Feather name="box" size={14} color={C.white} />
            <Text style={styles.heroBadgeTxt}>Bodega Central · Turno Matutino</Text>
          </View>
          <Feather name={isOnline ? "cloud" : "cloud-off"} size={18} color="#BAD6F0" />
        </View>
        <Text style={styles.heroTitle}>{totalItems.toLocaleString("es-MX")}</Text>
        <Text style={styles.heroSub}>unidades registradas en inventario</Text>

        <View style={styles.heroDivider} />
        <View style={styles.heroStats}>
          <View style={styles.heroStat}>
            <Text style={styles.heroStatV}>{skuCount}</Text>
            <Text style={styles.heroStatL}>SKUs activos</Text>
          </View>
          <View style={styles.heroSep} />
          <View style={styles.heroStat}>
            <Text style={styles.heroStatV}>{todayMoves}</Text>
            <Text style={styles.heroStatL}>Movimientos hoy</Text>
          </View>
          <View style={styles.heroSep} />
          <View style={styles.heroStat}>
            <Text style={styles.heroStatV}>{pending.length}</Text>
            <Text style={styles.heroStatL}>Sin sincronizar</Text>
          </View>
        </View>
      </View>

      <View style={styles.gridRow}>
        <StatCard
          icon="alert-triangle"
          color={C.error}
          bg={C.errorBg}
          label="Bajo stock"
          value={lowStock.toString()}
          sub="productos bajo mínimo"
        />
        <StatCard
          icon="clock"
          color={C.warn}
          bg={C.warnBg}
          label="Pendiente"
          value={pending.length.toString()}
          sub="por aprobar"
        />
      </View>

      <Text style={styles.sectionTitle}>Acciones rápidas</Text>
      <View style={styles.actionsRow}>
        <TouchableOpacity
          testID="quick-scan-btn"
          style={styles.actionCard}
          onPress={() => router.push("/(tabs)/scanner")}
        >
          <View style={[styles.actionIcon, { backgroundColor: "#EEF4FB" }]}>
            <Feather name="maximize" size={22} color={C.primary} />
          </View>
          <Text style={styles.actionTitle}>Escanear</Text>
          <Text style={styles.actionSub}>Registrar entrada / salida</Text>
        </TouchableOpacity>

        {isAdmin ? (
          <TouchableOpacity
            testID="quick-sync-btn"
            style={styles.actionCard}
            onPress={() => router.push("/(tabs)/sync")}
          >
            <View style={[styles.actionIcon, { backgroundColor: "#FEF3C7" }]}>
              <Feather name="refresh-cw" size={22} color={C.warn} />
            </View>
            <Text style={styles.actionTitle}>Revisar sync</Text>
            <Text style={styles.actionSub}>{pending.length} pendientes</Text>
          </TouchableOpacity>
        ) : (
          <View style={[styles.actionCard, { opacity: 0.55 }]}>
            <View style={[styles.actionIcon, { backgroundColor: "#F1F5F9" }]}>
              <Feather name="lock" size={20} color={C.gray500} />
            </View>
            <Text style={styles.actionTitle}>Admin</Text>
            <Text style={styles.actionSub}>Solo administrador</Text>
          </View>
        )}
      </View>

      <View style={styles.sectionHeader}>
        <Text style={styles.sectionTitle}>Actividad reciente</Text>
        {isAdmin && (
          <TouchableOpacity onPress={() => router.push("/(tabs)/history")}>
            <Text style={styles.linkTxt}>Ver historial →</Text>
          </TouchableOpacity>
        )}
      </View>

      {movements.slice(0, 4).map((m) => (
        <View key={m.id} style={styles.moveCard}>
          <View
            style={[
              styles.moveIcon,
              { backgroundColor: m.type === "entrada" ? C.successBg : C.errorBg },
            ]}
          >
            <Feather
              name={m.type === "entrada" ? "arrow-down-left" : "arrow-up-right"}
              size={16}
              color={m.type === "entrada" ? C.success : C.error}
            />
          </View>
          <View style={{ flex: 1 }}>
            <Text style={styles.moveName} numberOfLines={1}>{m.productName}</Text>
            <Text style={styles.moveMeta}>
              {m.sku} · {m.userName} · {m.timestamp}
            </Text>
          </View>
          <View style={{ alignItems: "flex-end" }}>
            <Text
              style={[
                styles.moveQty,
                { color: m.type === "entrada" ? C.success : C.error },
              ]}
            >
              {m.type === "entrada" ? "+" : "−"}{m.quantity}
            </Text>
            <Text style={styles.moveSource}>{m.source === "offline" ? "Offline" : "Online"}</Text>
          </View>
        </View>
      ))}

      <View style={{ height: 24 }} />
    </ScrollView>
  );
}

function StatCard({ icon, color, bg, label, value, sub }: any) {
  return (
    <View style={styles.statCard}>
      <View style={[styles.statIconWrap, { backgroundColor: bg }]}>
        <Feather name={icon} size={16} color={color} />
      </View>
      <Text style={styles.statLabel}>{label}</Text>
      <Text style={styles.statValue}>{value}</Text>
      <Text style={styles.statSub}>{sub}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: C.bg },
  content: { padding: 20, paddingBottom: 40 },
  headerRow: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", marginBottom: 20 },
  hi: { fontSize: 22, fontWeight: "800", color: C.gray800, letterSpacing: -0.5 },
  roleTxt: { fontSize: 12, color: C.gray500, marginTop: 2, fontWeight: "500" },
  logout: { width: 40, height: 40, borderRadius: 20, borderWidth: 1, borderColor: C.border, alignItems: "center", justifyContent: "center", backgroundColor: C.white },
  heroCard: { borderRadius: 20, padding: 20, marginBottom: 16 },
  heroTop: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", marginBottom: 20 },
  heroBadge: { flexDirection: "row", alignItems: "center", gap: 6, backgroundColor: "rgba(255,255,255,0.15)", paddingHorizontal: 10, paddingVertical: 5, borderRadius: 20 },
  heroBadgeTxt: { color: C.white, fontSize: 11, fontWeight: "600" },
  heroTitle: { color: C.white, fontSize: 44, fontWeight: "800", letterSpacing: -1 },
  heroSub: { color: "rgba(255,255,255,0.7)", fontSize: 13, marginTop: 2 },
  heroDivider: { height: 1, backgroundColor: "rgba(255,255,255,0.15)", marginVertical: 16 },
  heroStats: { flexDirection: "row", alignItems: "center" },
  heroStat: { flex: 1 },
  heroStatV: { color: C.white, fontSize: 18, fontWeight: "700" },
  heroStatL: { color: "rgba(255,255,255,0.65)", fontSize: 11, marginTop: 2 },
  heroSep: { width: 1, height: 30, backgroundColor: "rgba(255,255,255,0.15)" },
  gridRow: { flexDirection: "row", gap: 12, marginBottom: 8 },
  statCard: { flex: 1, backgroundColor: C.white, padding: 14, borderRadius: 14, borderWidth: 1, borderColor: C.border },
  statIconWrap: { width: 34, height: 34, borderRadius: 10, alignItems: "center", justifyContent: "center", marginBottom: 10 },
  statLabel: { fontSize: 11, fontWeight: "700", color: C.gray500, textTransform: "uppercase", letterSpacing: 0.8 },
  statValue: { fontSize: 24, fontWeight: "800", color: C.gray800, marginTop: 2 },
  statSub: { fontSize: 11, color: C.gray500, marginTop: 2 },
  sectionTitle: { fontSize: 15, fontWeight: "700", color: C.gray800, marginTop: 22, marginBottom: 10 },
  sectionHeader: { flexDirection: "row", justifyContent: "space-between", alignItems: "flex-end" },
  linkTxt: { color: C.primary, fontWeight: "600", fontSize: 13, marginBottom: 10 },
  actionsRow: { flexDirection: "row", gap: 12 },
  actionCard: { flex: 1, backgroundColor: C.white, padding: 16, borderRadius: 14, borderWidth: 1, borderColor: C.border },
  actionIcon: { width: 42, height: 42, borderRadius: 12, alignItems: "center", justifyContent: "center", marginBottom: 12 },
  actionTitle: { fontSize: 15, fontWeight: "700", color: C.gray800 },
  actionSub: { fontSize: 12, color: C.gray500, marginTop: 2 },
  moveCard: { flexDirection: "row", alignItems: "center", gap: 12, backgroundColor: C.white, padding: 14, borderRadius: 14, borderWidth: 1, borderColor: C.border, marginBottom: 8 },
  moveIcon: { width: 36, height: 36, borderRadius: 18, alignItems: "center", justifyContent: "center" },
  moveName: { fontSize: 14, fontWeight: "600", color: C.gray800 },
  moveMeta: { fontSize: 11, color: C.gray500, marginTop: 2 },
  moveQty: { fontSize: 15, fontWeight: "800" },
  moveSource: { fontSize: 10, color: C.gray500, marginTop: 2 },
});
