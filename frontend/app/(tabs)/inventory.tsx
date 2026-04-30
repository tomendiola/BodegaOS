import React, { useMemo, useState } from "react";
import { View, Text, StyleSheet, TextInput, ScrollView, TouchableOpacity, FlatList } from "react-native";
import { Feather } from "@expo/vector-icons";
import { router } from "expo-router";
import { useApp, Product } from "../../context/AppContext";

const C = {
  primary: "#0F4C81", white: "#FFFFFF", bg: "#F8FAFC", border: "#E2E8F0",
  gray300: "#CBD5E1", gray500: "#64748B", gray800: "#1E293B",
  success: "#16A34A", successBg: "#DCFCE7",
  error: "#DC2626", errorBg: "#FEE2E2",
  warn: "#F59E0B", warnBg: "#FEF3C7",
};

const FILTERS = ["Todos", "Bajo stock", "EPP", "Herramientas", "Eléctrico"];

export default function Inventory() {
  const { products } = useApp();
  const [q, setQ] = useState("");
  const [filter, setFilter] = useState("Todos");

  const filtered = useMemo(() => {
    let list = products;
    if (filter === "Bajo stock") list = list.filter((p) => p.quantity < p.minStock);
    else if (filter !== "Todos") list = list.filter((p) => p.category === filter);
    if (q.trim()) {
      const s = q.toLowerCase();
      list = list.filter((p) => p.name.toLowerCase().includes(s) || p.sku.toLowerCase().includes(s));
    }
    return list;
  }, [products, q, filter]);

  return (
    <View style={styles.screen}>
      <View style={styles.header}>
        <View>
          <Text style={styles.title}>Inventario</Text>
          <Text style={styles.sub}>{filtered.length} productos · {products.reduce((a, p) => a + p.quantity, 0)} unidades</Text>
        </View>
        <TouchableOpacity style={styles.iconBtn} testID="export-btn">
          <Feather name="download" size={16} color={C.gray800} />
        </TouchableOpacity>
      </View>

      <View style={styles.searchWrap}>
        <Feather name="search" size={16} color={C.gray500} />
        <TextInput
          testID="inventory-search-input"
          placeholder="Buscar por nombre o SKU..."
          placeholderTextColor="#94A3B8"
          value={q}
          onChangeText={setQ}
          style={styles.search}
        />
        {q ? (
          <TouchableOpacity onPress={() => setQ("")}>
            <Feather name="x" size={16} color={C.gray500} />
          </TouchableOpacity>
        ) : null}
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
            testID={`filter-${f}`}
          >
            <Text style={[styles.chipTxt, filter === f && styles.chipTxtActive]}>{f}</Text>
          </TouchableOpacity>
        ))}
      </ScrollView>

      <FlatList
        data={filtered}
        keyExtractor={(i) => i.id}
        contentContainerStyle={{ padding: 20, paddingTop: 4, paddingBottom: 40 }}
        renderItem={({ item }) => <ProductRow product={item} />}
        ItemSeparatorComponent={() => <View style={{ height: 10 }} />}
        ListEmptyComponent={
          <View style={styles.empty}>
            <Feather name="inbox" size={32} color={C.gray300} />
            <Text style={styles.emptyTxt}>Sin resultados</Text>
          </View>
        }
      />
    </View>
  );
}

function ProductRow({ product }: { product: Product }) {
  const low = product.quantity < product.minStock;
  return (
    <TouchableOpacity
      style={styles.row}
      onPress={() => router.push(`/product/${product.id}`)}
      testID={`product-${product.id}`}
      activeOpacity={0.8}
    >
      <View style={[styles.thumb, { backgroundColor: low ? C.errorBg : "#EEF4FB" }]}>
        <Feather
          name={low ? "alert-triangle" : "package"}
          size={18}
          color={low ? C.error : C.primary}
        />
      </View>
      <View style={{ flex: 1 }}>
        <Text style={styles.rowName} numberOfLines={1}>{product.name}</Text>
        <View style={styles.rowMetaRow}>
          <Text style={styles.rowSku}>{product.sku}</Text>
          <View style={styles.dotSep} />
          <Text style={styles.rowMeta}>{product.location}</Text>
          <View style={styles.dotSep} />
          <Text style={styles.rowMeta}>{product.lastUpdated}</Text>
        </View>
      </View>
      <View style={{ alignItems: "flex-end" }}>
        <Text style={[styles.qty, { color: low ? C.error : C.gray800 }]}>{product.quantity}</Text>
        {low ? (
          <View style={styles.lowBadge}>
            <Text style={styles.lowTxt}>BAJO</Text>
          </View>
        ) : (
          <Text style={styles.unit}>unidades</Text>
        )}
      </View>
      <Feather name="chevron-right" size={18} color={C.gray300} />
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: C.bg },
  header: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", padding: 20, paddingBottom: 10 },
  title: { fontSize: 24, fontWeight: "800", color: C.gray800, letterSpacing: -0.5 },
  sub: { fontSize: 12, color: C.gray500, marginTop: 2 },
  iconBtn: { width: 40, height: 40, borderRadius: 20, borderWidth: 1, borderColor: C.border, alignItems: "center", justifyContent: "center", backgroundColor: C.white },
  searchWrap: { marginHorizontal: 20, flexDirection: "row", alignItems: "center", gap: 10, backgroundColor: C.white, borderRadius: 12, borderWidth: 1, borderColor: C.border, paddingHorizontal: 14, height: 46 },
  search: { flex: 1, fontSize: 14, color: C.gray800 },
  chipsRow: { paddingHorizontal: 20, paddingVertical: 14, gap: 8 },
  chip: { paddingHorizontal: 14, paddingVertical: 8, borderRadius: 20, borderWidth: 1, borderColor: C.border, backgroundColor: C.white, marginRight: 8 },
  chipActive: { backgroundColor: C.primary, borderColor: C.primary },
  chipTxt: { fontSize: 12, fontWeight: "600", color: C.gray800 },
  chipTxtActive: { color: C.white },
  row: { flexDirection: "row", alignItems: "center", gap: 12, backgroundColor: C.white, padding: 14, borderRadius: 14, borderWidth: 1, borderColor: C.border },
  thumb: { width: 44, height: 44, borderRadius: 12, alignItems: "center", justifyContent: "center" },
  rowName: { fontSize: 14, fontWeight: "700", color: C.gray800 },
  rowMetaRow: { flexDirection: "row", alignItems: "center", gap: 6, marginTop: 3, flexWrap: "wrap" },
  rowSku: { fontSize: 11, fontWeight: "700", color: C.primary, letterSpacing: 0.5 },
  dotSep: { width: 3, height: 3, borderRadius: 1.5, backgroundColor: C.gray300 },
  rowMeta: { fontSize: 11, color: C.gray500 },
  qty: { fontSize: 18, fontWeight: "800" },
  unit: { fontSize: 10, color: C.gray500, marginTop: 2 },
  lowBadge: { marginTop: 2, backgroundColor: C.errorBg, paddingHorizontal: 6, paddingVertical: 2, borderRadius: 4 },
  lowTxt: { color: C.error, fontSize: 9, fontWeight: "800", letterSpacing: 1 },
  empty: { alignItems: "center", padding: 40 },
  emptyTxt: { marginTop: 8, color: C.gray500 },
});
