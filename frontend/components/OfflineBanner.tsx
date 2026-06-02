import React from "react";
import { View, Text, StyleSheet } from "react-native";
import { Feather } from "@expo/vector-icons";
import { useApp } from "../context/AppContext";

export default function OfflineBanner() {
  const { isOnline, pending } = useApp();

  // Si está online, no mostramos el banner para que sea más limpio
  if (isOnline) return null;

  return (
    <View style={styles.wrap}>
      <View style={[styles.banner, styles.bannerOffline]}>
        <View style={styles.left}>
          <View style={[styles.dot, { backgroundColor: "#F59E0B" }]} />
          <Feather
            name="wifi-off"
            size={14}
            color="#92400E"
          />
          <Text style={[styles.txt, { color: "#92400E" }]}>
            Modo offline · {pending.length} pendiente(s) por sincronizar
          </Text>
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { backgroundColor: "#FFFFFF" },
  banner: {
    flexDirection: "row", alignItems: "center", justifyContent: "space-between",
    paddingHorizontal: 16, paddingVertical: 8, borderBottomWidth: 1,
  },
  bannerOffline: { backgroundColor: "#FEF3C7", borderBottomColor: "#FDE68A" },
  left: { flexDirection: "row", alignItems: "center", gap: 8, flex: 1 },
  dot: { width: 8, height: 8, borderRadius: 4 },
  txt: { fontSize: 12, fontWeight: "600", flexShrink: 1 },
});
