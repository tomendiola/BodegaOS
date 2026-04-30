import React from "react";
import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Feather } from "@expo/vector-icons";
import { router, useLocalSearchParams } from "expo-router";
import { useApp } from "../../context/AppContext";

const C = {
  primary: "#0F4C81", primaryLight: "#2A6BA6", primaryDark: "#0B3960",
  white: "#FFFFFF", bg: "#F8FAFC", border: "#E2E8F0",
  gray300: "#CBD5E1", gray500: "#64748B", gray800: "#1E293B",
  success: "#16A34A", successBg: "#DCFCE7",
  error: "#DC2626", errorBg: "#FEE2E2",
  warn: "#F59E0B", warnBg: "#FEF3C7",
};

export default function ProductDetail() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { products, movements } = useApp();
  const product = products.find((p) => p.id === id);
  const productMoves = movements.filter((m) => m.productId === id);

  if (!product) {
    return (
      <SafeAreaView style={styles.screen}>
        <Text style={styles.title}>Producto no encontrado</Text>
      </SafeAreaView>
    );
  }

  const low = product.quantity < product.minStock;
  const entries = productMoves.filter((m) => m.type === "entrada").reduce((a, m) => a + m.quantity, 0);
  const exits = productMoves.filter((m) => m.type === "salida").reduce((a, m) => a + m.quantity, 0);

  return (
    <SafeAreaView style={styles.screen} edges={["top"]}>
      <View style={styles.header}>
        <TouchableOpacity style={styles.backBtn} onPress={() => router.back()} testID="back-btn">
          <Feather name="arrow-left" size={20} color={C.gray800} />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Detalle del producto</Text>
        <TouchableOpacity style={styles.backBtn}>
          <Feather name="more-vertical" size={20} color={C.gray800} />
        </TouchableOpacity>
      </View>

      <ScrollView contentContainerStyle={{ padding: 20, paddingBottom: 40 }}>
        <View style={styles.heroCard}>
          <View style={styles.heroIcon}>
            <Feather name="package" size={32} color={C.white} />
          </View>
          <Text style={styles.sku}>{product.sku}</Text>
          <Text style={styles.name}>{product.name}</Text>
          <View style={styles.catTag}>
            <Feather name="tag" size={11} color={C.white} />
            <Text style={styles.catTxt}>{product.category}</Text>
          </View>
        </View>

        <View style={[styles.stockCard, low && { borderColor: "#FCA5A5" }]}>
          <View style={styles.stockTop}>
            <Text style={styles.stockLabel}>STOCK ACTUAL</Text>
            {low && (
              <View style={styles.lowBadge}>
                <Feather name="alert-triangle" size={10} color={C.error} />
                <Text style={styles.lowTxt}>Bajo mínimo</Text>
              </View>
            )}
          </View>
          <View style={styles.stockRow}>
            <Text style={[styles.stockValue, low && { color: C.error }]}>{product.quantity}</Text>
            <Text style={styles.stockUnit}>unidades</Text>
          </View>
          <View style={styles.stockBarBg}>
            <View
              style={[
                styles.stockBar,
                {
                  width: `${Math.min(100, (product.quantity / (product.minStock * 3)) * 100)}%`,
                  backgroundColor: low ? C.error : C.success,
                },
              ]}
            />
          </View>
          <Text style={styles.stockMin}>Mínimo requerido: {product.minStock} unidades</Text>
        </View>

        <View style={styles.metaGrid}>
          <MetaCard icon="map-pin" label="Ubicación" value={product.location} />
          <MetaCard icon="clock" label="Última actualización" value={product.lastUpdated} />
          <MetaCard icon="arrow-down-left" label="Entradas totales" value={`+${entries}`} color={C.success} />
          <MetaCard icon="arrow-up-right" label="Salidas totales" value={`−${exits}`} color={C.error} />
        </View>

        <Text style={styles.sectionTitle}>Historial del producto</Text>

        {productMoves.length === 0 ? (
          <View style={styles.empty}>
            <Feather name="inbox" size={24} color={C.gray300} />
            <Text style={styles.emptyTxt}>Sin movimientos registrados</Text>
          </View>
        ) : (
          productMoves.map((m) => (
            <View key={m.id} style={styles.mRow}>
              <View
                style={[
                  styles.mIcon,
                  {
                    backgroundColor:
                      m.status === "rechazado"
                        ? "#F1F5F9"
                        : m.type === "entrada"
                        ? C.successBg
                        : C.errorBg,
                  },
                ]}
              >
                <Feather
                  name={m.type === "entrada" ? "arrow-down-left" : "arrow-up-right"}
                  size={14}
                  color={
                    m.status === "rechazado"
                      ? C.gray500
                      : m.type === "entrada"
                      ? C.success
                      : C.error
                  }
                />
              </View>
              <View style={{ flex: 1 }}>
                <Text style={styles.mUser}>{m.userName}</Text>
                <Text style={styles.mTime}>{m.timestamp} · {m.source}</Text>
              </View>
              <Text
                style={[
                  styles.mQty,
                  {
                    color:
                      m.status === "rechazado"
                        ? C.gray500
                        : m.type === "entrada"
                        ? C.success
                        : C.error,
                  },
                ]}
              >
                {m.type === "entrada" ? "+" : "−"}{m.quantity}
              </Text>
            </View>
          ))
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

function MetaCard({ icon, label, value, color }: any) {
  return (
    <View style={styles.metaCard}>
      <Feather name={icon} size={14} color={C.gray500} />
      <Text style={styles.metaLabel}>{label}</Text>
      <Text style={[styles.metaValue, color && { color }]}>{value}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: C.bg },
  header: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", paddingHorizontal: 16, paddingVertical: 10, backgroundColor: C.white, borderBottomWidth: 1, borderBottomColor: C.border },
  backBtn: { width: 40, height: 40, borderRadius: 20, alignItems: "center", justifyContent: "center" },
  headerTitle: { fontSize: 15, fontWeight: "700", color: C.gray800 },
  title: { fontSize: 20, fontWeight: "800", color: C.gray800, padding: 20 },
  heroCard: { backgroundColor: C.primary, padding: 24, borderRadius: 20, marginBottom: 16, alignItems: "flex-start" },
  heroIcon: { width: 56, height: 56, borderRadius: 16, backgroundColor: "rgba(255,255,255,0.15)", alignItems: "center", justifyContent: "center", marginBottom: 14 },
  sku: { color: "rgba(255,255,255,0.7)", fontSize: 11, fontWeight: "800", letterSpacing: 1.2 },
  name: { color: C.white, fontSize: 24, fontWeight: "800", letterSpacing: -0.5, marginTop: 4, marginBottom: 12 },
  catTag: { flexDirection: "row", alignItems: "center", gap: 4, backgroundColor: "rgba(255,255,255,0.18)", paddingHorizontal: 10, paddingVertical: 5, borderRadius: 20 },
  catTxt: { color: C.white, fontSize: 11, fontWeight: "700" },
  stockCard: { backgroundColor: C.white, padding: 18, borderRadius: 16, borderWidth: 1, borderColor: C.border, marginBottom: 14 },
  stockTop: { flexDirection: "row", justifyContent: "space-between", alignItems: "center" },
  stockLabel: { fontSize: 10, fontWeight: "800", color: C.gray500, letterSpacing: 1.2 },
  lowBadge: { flexDirection: "row", alignItems: "center", gap: 4, backgroundColor: C.errorBg, paddingHorizontal: 8, paddingVertical: 3, borderRadius: 5 },
  lowTxt: { color: C.error, fontSize: 10, fontWeight: "800" },
  stockRow: { flexDirection: "row", alignItems: "baseline", gap: 8, marginTop: 6 },
  stockValue: { fontSize: 40, fontWeight: "800", color: C.gray800, letterSpacing: -1 },
  stockUnit: { fontSize: 13, color: C.gray500, fontWeight: "500" },
  stockBarBg: { height: 6, borderRadius: 3, backgroundColor: "#F1F5F9", marginTop: 10, overflow: "hidden" },
  stockBar: { height: "100%", borderRadius: 3 },
  stockMin: { fontSize: 11, color: C.gray500, marginTop: 6 },
  metaGrid: { flexDirection: "row", flexWrap: "wrap", gap: 10, marginBottom: 6 },
  metaCard: { flexBasis: "48%", flexGrow: 1, backgroundColor: C.white, padding: 12, borderRadius: 12, borderWidth: 1, borderColor: C.border },
  metaLabel: { fontSize: 10, fontWeight: "700", color: C.gray500, letterSpacing: 0.8, marginTop: 6, textTransform: "uppercase" },
  metaValue: { fontSize: 14, fontWeight: "700", color: C.gray800, marginTop: 2 },
  sectionTitle: { fontSize: 15, fontWeight: "700", color: C.gray800, marginTop: 20, marginBottom: 10 },
  mRow: { flexDirection: "row", alignItems: "center", gap: 12, backgroundColor: C.white, padding: 12, borderRadius: 12, borderWidth: 1, borderColor: C.border, marginBottom: 8 },
  mIcon: { width: 32, height: 32, borderRadius: 10, alignItems: "center", justifyContent: "center" },
  mUser: { fontSize: 13, fontWeight: "600", color: C.gray800 },
  mTime: { fontSize: 11, color: C.gray500, marginTop: 2 },
  mQty: { fontSize: 16, fontWeight: "800" },
  empty: { alignItems: "center", padding: 40, backgroundColor: C.white, borderRadius: 12, borderWidth: 1, borderColor: C.border },
  emptyTxt: { marginTop: 8, color: C.gray500, fontSize: 12 },
});
