import React, { useMemo, useState } from "react";
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, FlatList } from "react-native";
import { Feather } from "@expo/vector-icons";
import { useApp, Movement } from "../../context/AppContext";

const C = {
  primary: "#0F4C81", white: "#FFFFFF", bg: "#F8FAFC", border: "#E2E8F0",
  gray300: "#CBD5E1", gray500: "#64748B", gray800: "#1E293B",
  success: "#16A34A", successBg: "#DCFCE7",
  error: "#DC2626", errorBg: "#FEE2E2",
  warn: "#F59E0B", warnBg: "#FEF3C7",
};

const FILTERS = ["Todos", "Entradas", "Salidas", "Offline", "Rechazados"];

export default function History() {
  const { movements } = useApp();
  const [filter, setFilter] = useState("Todos");

  const filtered = useMemo(() => {
    if (filter === "Entradas") return movements.filter((m) => m.type === "entrada");
    if (filter === "Salidas") return movements.filter((m) => m.type === "salida");
    if (filter === "Offline") return movements.filter((m) => m.source === "offline");
    if (filter === "Rechazados") return movements.filter((m) => m.status === "rechazado");
    return movements;
  }, [movements, filter]);

  return (
    <View style={styles.screen}>
      <View style={styles.header}>
        <View>
          <Text style={styles.title}>Historial</Text>
          <Text style={styles.sub}>Trazabilidad de movimientos</Text>
        </View>
        <TouchableOpacity style={styles.iconBtn}>
          <Feather name="filter" size={16} color={C.gray800} />
        </TouchableOpacity>
      </View>

      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.chipsRow}
      >
        {FILTERS.map((f) => (
          <TouchableOpacity
            key={f}
            style={[styles.chip, filter === f && styles.chipActive]}
            onPress={() => setFilter(f)}
          >
            <Text style={[styles.chipTxt, filter === f && styles.chipTxtActive]}>{f}</Text>
          </TouchableOpacity>
        ))}
      </ScrollView>

      <FlatList
        data={filtered}
        keyExtractor={(i) => i.id}
        contentContainerStyle={{ padding: 20, paddingTop: 4, paddingBottom: 40 }}
        renderItem={({ item }) => <MovementRow m={item} />}
        ItemSeparatorComponent={() => <View style={{ height: 10 }} />}
      />
    </View>
  );
}

function MovementRow({ m }: { m: Movement }) {
  const isEntry = m.type === "entrada";
  const rejected = m.status === "rechazado";
  return (
    <View style={styles.row}>
      <View
        style={[
          styles.iconW,
          { backgroundColor: rejected ? "#F1F5F9" : isEntry ? C.successBg : C.errorBg },
        ]}
      >
        <Feather
          name={rejected ? "x" : isEntry ? "arrow-down-left" : "arrow-up-right"}
          size={16}
          color={rejected ? C.gray500 : isEntry ? C.success : C.error}
        />
      </View>
      <View style={{ flex: 1 }}>
        <Text style={styles.name} numberOfLines={1}>{m.productName}</Text>
        <View style={styles.metaRow}>
          <Text style={styles.sku}>{m.sku}</Text>
          <View style={styles.dotSep} />
          <Feather name="user" size={10} color={C.gray500} />
          <Text style={styles.meta}>{m.userName}</Text>
          <View style={styles.dotSep} />
          <Text style={styles.meta}>{m.timestamp}</Text>
        </View>
        <View style={styles.tagsRow}>
          <View
            style={[
              styles.tag,
              { backgroundColor: m.source === "online" ? "#EEF4FB" : C.warnBg },
            ]}
          >
            <Feather
              name={m.source === "online" ? "wifi" : "wifi-off"}
              size={9}
              color={m.source === "online" ? C.primary : "#92400E"}
            />
            <Text
              style={[
                styles.tagTxt,
                { color: m.source === "online" ? C.primary : "#92400E" },
              ]}
            >
              {m.source}
            </Text>
          </View>
          {rejected && (
            <View style={[styles.tag, { backgroundColor: C.errorBg }]}>
              <Text style={[styles.tagTxt, { color: C.error }]}>Rechazado</Text>
            </View>
          )}
        </View>
      </View>
      <Text
        style={[
          styles.qty,
          { color: rejected ? C.gray500 : isEntry ? C.success : C.error, textDecorationLine: rejected ? "line-through" : "none" },
        ]}
      >
        {isEntry ? "+" : "−"}{m.quantity}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: C.bg },
  header: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", padding: 20, paddingBottom: 10 },
  title: { fontSize: 24, fontWeight: "800", color: C.gray800, letterSpacing: -0.5 },
  sub: { fontSize: 12, color: C.gray500, marginTop: 2 },
  iconBtn: { width: 40, height: 40, borderRadius: 20, borderWidth: 1, borderColor: C.border, alignItems: "center", justifyContent: "center", backgroundColor: C.white },
  chipsRow: { paddingHorizontal: 20, paddingVertical: 8, gap: 8 },
  chip: { paddingHorizontal: 14, paddingVertical: 8, borderRadius: 20, borderWidth: 1, borderColor: C.border, backgroundColor: C.white, marginRight: 8 },
  chipActive: { backgroundColor: C.primary, borderColor: C.primary },
  chipTxt: { fontSize: 12, fontWeight: "600", color: C.gray800 },
  chipTxtActive: { color: C.white },
  row: { flexDirection: "row", alignItems: "center", gap: 12, backgroundColor: C.white, padding: 14, borderRadius: 14, borderWidth: 1, borderColor: C.border },
  iconW: { width: 40, height: 40, borderRadius: 12, alignItems: "center", justifyContent: "center" },
  name: { fontSize: 14, fontWeight: "700", color: C.gray800 },
  metaRow: { flexDirection: "row", alignItems: "center", gap: 5, marginTop: 4, flexWrap: "wrap" },
  sku: { fontSize: 11, fontWeight: "700", color: C.primary },
  dotSep: { width: 3, height: 3, borderRadius: 1.5, backgroundColor: C.gray300 },
  meta: { fontSize: 11, color: C.gray500 },
  tagsRow: { flexDirection: "row", gap: 6, marginTop: 6 },
  tag: { flexDirection: "row", alignItems: "center", gap: 4, paddingHorizontal: 7, paddingVertical: 3, borderRadius: 5 },
  tagTxt: { fontSize: 9, fontWeight: "800", textTransform: "uppercase", letterSpacing: 0.6 },
  qty: { fontSize: 18, fontWeight: "800" },
});
