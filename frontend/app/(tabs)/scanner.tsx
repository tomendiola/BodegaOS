import React, { useEffect, useRef, useState } from "react";
import { View, Text, StyleSheet, TouchableOpacity, Animated, Easing, Modal, TextInput } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Feather, Ionicons } from "@expo/vector-icons";
import { useApp } from "../../context/AppContext";

const C = {
  primary: "#0F4C81",
  white: "#FFFFFF",
  bg: "#F8FAFC",
  border: "#E2E8F0",
  gray300: "#CBD5E1",
  gray500: "#64748B",
  gray800: "#1E293B",
  success: "#16A34A",
  successBg: "#DCFCE7",
  error: "#DC2626",
  errorBg: "#FEE2E2",
  warn: "#F59E0B",
};

export default function Scanner() {
  const { products, addScan, isOnline } = useApp();
  const [scanned, setScanned] = useState<null | { productId: string }>(null);
  const [qty, setQty] = useState("1");
  const [feedback, setFeedback] = useState<string | null>(null);
  const laserY = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    const loop = Animated.loop(
      Animated.sequence([
        Animated.timing(laserY, { toValue: 1, duration: 1800, easing: Easing.inOut(Easing.ease), useNativeDriver: true }),
        Animated.timing(laserY, { toValue: 0, duration: 1800, easing: Easing.inOut(Easing.ease), useNativeDriver: true }),
      ])
    );
    loop.start();
    return () => loop.stop();
  }, [laserY]);

  const simulateScan = () => {
    const random = products[Math.floor(Math.random() * products.length)];
    setScanned({ productId: random.id });
    setQty("1");
  };

  const confirm = (type: "entrada" | "salida") => {
    if (!scanned) return;
    const q = Math.max(1, parseInt(qty || "1", 10) || 1);
    addScan(scanned.productId, type, q);
    const prod = products.find((p) => p.id === scanned.productId);
    setFeedback(
      isOnline
        ? `${type === "entrada" ? "+" : "−"}${q} · ${prod?.name} applied`
        : `Offline · enviado a revisión del admin`
    );
    setScanned(null);
    setTimeout(() => setFeedback(null), 2800);
  };

  const translateY = laserY.interpolate({ inputRange: [0, 1], outputRange: [0, 220] });
  const product = scanned ? products.find((p) => p.id === scanned.productId) : null;

  return (
    <View style={styles.root}>
      <View style={styles.camera}>
        {/* Simulated camera backdrop */}
        <View style={styles.cameraBg} />
        <View style={[styles.gridLine, { top: "25%" }]} />
        <View style={[styles.gridLine, { top: "50%" }]} />
        <View style={[styles.gridLine, { top: "75%" }]} />
        <View style={[styles.gridLineV, { left: "25%" }]} />
        <View style={[styles.gridLineV, { left: "50%" }]} />
        <View style={[styles.gridLineV, { left: "75%" }]} />

        {/* Top bar */}
        <View style={styles.topBar}>
          <View style={styles.tbBadge}>
            <View style={[styles.dot, { backgroundColor: C.success }]} />
            <Text style={styles.tbTxt}>Cámara activa · ML Kit</Text>
          </View>
          <TouchableOpacity style={styles.iconBtn}>
            <Feather name="zap" size={16} color={C.white} />
          </TouchableOpacity>
        </View>

        {/* Scanner frame */}
        <View style={styles.frameWrap} pointerEvents="none">
          <View style={styles.frame}>
            <View style={[styles.corner, styles.cornerTL]} />
            <View style={[styles.corner, styles.cornerTR]} />
            <View style={[styles.corner, styles.cornerBL]} />
            <View style={[styles.corner, styles.cornerBR]} />
            <Animated.View style={[styles.laser, { transform: [{ translateY }] }]} />
          </View>
          <Text style={styles.hint}>Apunta al código QR o de barras</Text>
        </View>

        {/* Feedback toast */}
        {feedback && (
          <View style={styles.toast} testID="scan-toast">
            <Feather
              name={isOnline ? "check-circle" : "clock"}
              size={16}
              color={isOnline ? C.success : C.warn}
            />
            <Text style={styles.toastTxt}>{feedback}</Text>
          </View>
        )}

        {/* Bottom controls */}
        <SafeAreaView style={styles.bottomBar} edges={["bottom"]}>
          <View style={styles.modeSwitch}>
            <View style={[styles.modeItem, styles.modeItemActive]}>
              <Ionicons name="qr-code-outline" size={16} color={C.primary} />
              <Text style={styles.modeTxtActive}>QR</Text>
            </View>
            <View style={styles.modeItem}>
              <Feather name="align-justify" size={16} color={C.white} />
              <Text style={styles.modeTxt}>Código de barras</Text>
            </View>
          </View>
          <TouchableOpacity
            testID="simulate-scan-btn"
            style={styles.shutter}
            onPress={simulateScan}
            activeOpacity={0.85}
          >
            <View style={styles.shutterInner} />
          </TouchableOpacity>
          <Text style={styles.shutterHint}>Tocar para simular un escaneo</Text>
        </SafeAreaView>
      </View>

      {/* Modal after scan */}
      <Modal visible={!!scanned} transparent animationType="slide" onRequestClose={() => setScanned(null)}>
        <View style={styles.modalBg}>
          <View style={styles.sheet}>
            <View style={styles.handle} />
            <View style={styles.sheetHeader}>
              <View style={styles.skuBadge}>
                <Feather name="check" size={12} color={C.success} />
                <Text style={styles.skuBadgeTxt}>Código detectado</Text>
              </View>
              <TouchableOpacity onPress={() => setScanned(null)} testID="close-scan-modal">
                <Feather name="x" size={22} color={C.gray500} />
              </TouchableOpacity>
            </View>

            {product && (
              <>
                <Text style={styles.prodSku}>{product.sku}</Text>
                <Text style={styles.prodName}>{product.name}</Text>
                <View style={styles.prodMetaRow}>
                  <View style={styles.metaItem}>
                    <Feather name="map-pin" size={12} color={C.gray500} />
                    <Text style={styles.metaTxt}>{product.location}</Text>
                  </View>
                  <View style={styles.metaItem}>
                    <Feather name="layers" size={12} color={C.gray500} />
                    <Text style={styles.metaTxt}>{product.category}</Text>
                  </View>
                  <View style={styles.metaItem}>
                    <Feather name="box" size={12} color={C.gray500} />
                    <Text style={styles.metaTxt}>Stock: {product.quantity}</Text>
                  </View>
                </View>

                <Text style={styles.qtyLabel}>Cantidad</Text>
                <View style={styles.qtyRow}>
                  <TouchableOpacity
                    style={styles.qtyBtn}
                    onPress={() => setQty((q) => String(Math.max(1, (parseInt(q || "1", 10) || 1) - 1)))}
                  >
                    <Feather name="minus" size={18} color={C.gray800} />
                  </TouchableOpacity>
                  <TextInput
                    testID="scan-qty-input"
                    style={styles.qtyInput}
                    value={qty}
                    onChangeText={(t) => setQty(t.replace(/[^0-9]/g, ""))}
                    keyboardType="number-pad"
                  />
                  <TouchableOpacity
                    style={styles.qtyBtn}
                    onPress={() => setQty((q) => String((parseInt(q || "0", 10) || 0) + 1))}
                  >
                    <Feather name="plus" size={18} color={C.gray800} />
                  </TouchableOpacity>
                </View>

                {!isOnline && (
                  <View style={styles.offWarn}>
                    <Feather name="wifi-off" size={14} color="#92400E" />
                    <Text style={styles.offWarnTxt}>
                      Sin conexión · esta operación irá a revisión del administrador antes de aplicarse al inventario.
                    </Text>
                  </View>
                )}

                <View style={styles.actionsRow}>
                  <TouchableOpacity
                    testID="confirm-salida-btn"
                    style={[styles.confirmBtn, { backgroundColor: C.errorBg }]}
                    onPress={() => confirm("salida")}
                  >
                    <Feather name="arrow-up-right" size={16} color={C.error} />
                    <Text style={[styles.confirmTxt, { color: C.error }]}>Salida</Text>
                  </TouchableOpacity>
                  <TouchableOpacity
                    testID="confirm-entrada-btn"
                    style={[styles.confirmBtn, { backgroundColor: C.successBg }]}
                    onPress={() => confirm("entrada")}
                  >
                    <Feather name="arrow-down-left" size={16} color={C.success} />
                    <Text style={[styles.confirmTxt, { color: C.success }]}>Entrada</Text>
                  </TouchableOpacity>
                </View>
              </>
            )}
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: "#000" },
  camera: { flex: 1, overflow: "hidden" },
  cameraBg: { ...StyleSheet.absoluteFillObject, backgroundColor: "#0B1220" },
  gridLine: { position: "absolute", left: 0, right: 0, height: 1, backgroundColor: "rgba(255,255,255,0.04)" },
  gridLineV: { position: "absolute", top: 0, bottom: 0, width: 1, backgroundColor: "rgba(255,255,255,0.04)" },
  topBar: {
    flexDirection: "row", justifyContent: "space-between", alignItems: "center",
    paddingHorizontal: 16, paddingTop: 12,
  },
  tbBadge: { flexDirection: "row", alignItems: "center", gap: 8, backgroundColor: "rgba(0,0,0,0.55)", paddingHorizontal: 10, paddingVertical: 6, borderRadius: 20, borderWidth: 1, borderColor: "rgba(255,255,255,0.15)" },
  dot: { width: 8, height: 8, borderRadius: 4 },
  tbTxt: { color: C.white, fontSize: 11, fontWeight: "600" },
  iconBtn: { width: 36, height: 36, borderRadius: 18, backgroundColor: "rgba(0,0,0,0.55)", alignItems: "center", justifyContent: "center", borderWidth: 1, borderColor: "rgba(255,255,255,0.15)" },
  frameWrap: { flex: 1, alignItems: "center", justifyContent: "center" },
  frame: { width: 240, height: 240, borderRadius: 24, overflow: "hidden" },
  corner: { position: "absolute", width: 28, height: 28, borderColor: C.success, borderWidth: 3 },
  cornerTL: { top: 0, left: 0, borderRightWidth: 0, borderBottomWidth: 0, borderTopLeftRadius: 18 },
  cornerTR: { top: 0, right: 0, borderLeftWidth: 0, borderBottomWidth: 0, borderTopRightRadius: 18 },
  cornerBL: { bottom: 0, left: 0, borderRightWidth: 0, borderTopWidth: 0, borderBottomLeftRadius: 18 },
  cornerBR: { bottom: 0, right: 0, borderLeftWidth: 0, borderTopWidth: 0, borderBottomRightRadius: 18 },
  laser: { position: "absolute", left: 8, right: 8, height: 2, backgroundColor: C.success, shadowColor: C.success, shadowOpacity: 0.8, shadowRadius: 8 },
  hint: { color: "rgba(255,255,255,0.7)", fontSize: 13, marginTop: 18 },
  toast: { position: "absolute", top: 72, alignSelf: "center", flexDirection: "row", alignItems: "center", gap: 8, backgroundColor: "rgba(0,0,0,0.85)", paddingHorizontal: 14, paddingVertical: 10, borderRadius: 12, borderWidth: 1, borderColor: "rgba(255,255,255,0.15)" },
  toastTxt: { color: C.white, fontSize: 13, fontWeight: "600" },
  bottomBar: { padding: 20, alignItems: "center" },
  modeSwitch: { flexDirection: "row", backgroundColor: "rgba(255,255,255,0.08)", borderRadius: 12, padding: 4, marginBottom: 22 },
  modeItem: { flexDirection: "row", alignItems: "center", gap: 6, paddingHorizontal: 14, paddingVertical: 8, borderRadius: 9 },
  modeItemActive: { backgroundColor: C.white },
  modeTxt: { color: C.white, fontSize: 12, fontWeight: "600" },
  modeTxtActive: { color: C.primary, fontSize: 12, fontWeight: "700" },
  shutter: { width: 78, height: 78, borderRadius: 39, borderWidth: 4, borderColor: C.white, alignItems: "center", justifyContent: "center" },
  shutterInner: { width: 58, height: 58, borderRadius: 29, backgroundColor: C.white },
  shutterHint: { color: "rgba(255,255,255,0.55)", fontSize: 11, marginTop: 10 },
  modalBg: { flex: 1, backgroundColor: "rgba(15,23,42,0.55)", justifyContent: "flex-end" },
  sheet: { backgroundColor: C.white, borderTopLeftRadius: 24, borderTopRightRadius: 24, padding: 20, paddingBottom: 30 },
  handle: { width: 44, height: 5, borderRadius: 3, backgroundColor: C.gray300, alignSelf: "center", marginBottom: 14 },
  sheetHeader: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", marginBottom: 12 },
  skuBadge: { flexDirection: "row", alignItems: "center", gap: 6, backgroundColor: C.successBg, paddingHorizontal: 10, paddingVertical: 5, borderRadius: 20 },
  skuBadgeTxt: { color: C.success, fontSize: 11, fontWeight: "700" },
  prodSku: { color: C.gray500, fontSize: 12, fontWeight: "700", letterSpacing: 1, textTransform: "uppercase" },
  prodName: { color: C.gray800, fontSize: 20, fontWeight: "800", marginTop: 4 },
  prodMetaRow: { flexDirection: "row", gap: 14, marginTop: 10, flexWrap: "wrap" },
  metaItem: { flexDirection: "row", alignItems: "center", gap: 4 },
  metaTxt: { color: C.gray500, fontSize: 12, fontWeight: "500" },
  qtyLabel: { fontSize: 11, fontWeight: "700", color: C.gray500, marginTop: 18, marginBottom: 8, letterSpacing: 1, textTransform: "uppercase" },
  qtyRow: { flexDirection: "row", alignItems: "center", gap: 10 },
  qtyBtn: { width: 44, height: 44, borderRadius: 12, borderWidth: 1, borderColor: C.border, alignItems: "center", justifyContent: "center", backgroundColor: C.bg },
  qtyInput: { flex: 1, height: 44, borderRadius: 12, borderWidth: 1, borderColor: C.border, textAlign: "center", fontSize: 18, fontWeight: "700", color: C.gray800, backgroundColor: C.bg },
  offWarn: { flexDirection: "row", alignItems: "center", gap: 8, backgroundColor: "#FEF3C7", padding: 12, borderRadius: 10, marginTop: 14 },
  offWarnTxt: { flex: 1, color: "#92400E", fontSize: 12, fontWeight: "500", lineHeight: 16 },
  actionsRow: { flexDirection: "row", gap: 10, marginTop: 16 },
  confirmBtn: { flex: 1, height: 52, borderRadius: 14, flexDirection: "row", alignItems: "center", justifyContent: "center", gap: 8 },
  confirmTxt: { fontSize: 15, fontWeight: "700" },
});
