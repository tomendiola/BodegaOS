import React from "react";
import { View, Text, StyleSheet, TouchableOpacity } from "react-native";
import { Feather } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useApp } from "../context/AppContext";

export default function OfflineBanner() {
  const { isOnline, toggleOnline, pending } = useApp();
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.wrap, { paddingTop: insets.top }]}>
      <View style={[styles.banner, isOnline ? styles.bannerOnline : styles.bannerOffline]}>
        <View style={styles.left}>
          <View style={[styles.dot, { backgroundColor: isOnline ? "#16A34A" : "#F59E0B" }]} />
          <Feather
            name={isOnline ? "wifi" : "wifi-off"}
            size={14}
            color={isOnline ? "#065F46" : "#92400E"}
          />
          <Text style={[styles.txt, { color: isOnline ? "#065F46" : "#92400E" }]}>
            {isOnline ? "Conectado · Sincronización automática" : `Modo offline · ${pending.length} pendiente(s) por revisar`}
          </Text>
        </View>
        <TouchableOpacity
          testID="offline-toggle-button"
          onPress={toggleOnline}
          style={styles.toggle}
          hitSlop={10}
        >
          <Text style={[styles.toggleTxt, { color: isOnline ? "#065F46" : "#92400E" }]}>
            {isOnline ? "Simular offline" : "Reconectar"}
          </Text>
        </TouchableOpacity>
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
  bannerOnline: { backgroundColor: "#DCFCE7", borderBottomColor: "#BBF7D0" },
  bannerOffline: { backgroundColor: "#FEF3C7", borderBottomColor: "#FDE68A" },
  left: { flexDirection: "row", alignItems: "center", gap: 8, flex: 1 },
  dot: { width: 8, height: 8, borderRadius: 4 },
  txt: { fontSize: 12, fontWeight: "600", flexShrink: 1 },
  toggle: { paddingHorizontal: 10, paddingVertical: 4 },
  toggleTxt: { fontSize: 11, fontWeight: "700", textDecorationLine: "underline" },
});
