import React, { useState } from "react";
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, Alert, Platform } from "react-native";
import { Feather } from "@expo/vector-icons";
import { useApp, PendingSync } from "../../context/AppContext";

const C = {
  primary: "#0F4C81", white: "#FFFFFF", bg: "#F8FAFC", border: "#E2E8F0",
  gray300: "#CBD5E1", gray500: "#64748B", gray800: "#1E293B",
  success: "#16A34A", successBg: "#DCFCE7",
  error: "#DC2626", errorBg: "#FEE2E2",
  warn: "#F59E0B", warnBg: "#FEF3C7",
};

export default function Sync() {
  const { pending, approvePending, rejectPending, isOnline } = useApp();
  const [toast, setToast] = useState<string | null>(null);

  const showToast = (t: string) => {
    setToast(t);
    setTimeout(() => setToast(null), 2400);
  };

  const onApprove = (id: string, name: string) => {
    approvePending(id);
    showToast(`Aprobado · ${name} aplicado al inventario`);
  };

  const onReject = (id: string, name: string) => {
    const doReject = () => {
      rejectPending(id);
      showToast(`Rechazado · ${name}`);
    };
    if (Platform.OS === "web") {
      doReject();
    } else {
      Alert.alert(
        "Rechazar operación",
        `¿Rechazar la operación pendiente para ${name}? No se aplicará al inventario.`,
        [
          { text: "Cancelar", style: "cancel" },
          { text: "Rechazar", style: "destructive", onPress: doReject },
        ]
      );
    }
  };

  const approveAll = () => {
    if (!pending.length) return;
    pending.forEach((p) => approvePending(p.id));
    showToast(`${pending.length} operaciones aplicadas`);
  };

  return (
    <View style={styles.screen}>
      <ScrollView contentContainerStyle={{ padding: 20, paddingBottom: 40 }}>
        <Text style={styles.title}>Sincronización</Text>
        <Text style={styles.sub}>Operaciones capturadas sin conexión</Text>

        <View style={[styles.status, { borderColor: isOnline ? "#BBF7D0" : "#FDE68A", backgroundColor: isOnline ? C.successBg : C.warnBg }]}>
          <View style={[styles.statusIcon, { backgroundColor: isOnline ? "#BBF7D0" : "#FDE68A" }]}>
            <Feather name={isOnline ? "cloud" : "cloud-off"} size={18} color={isOnline ? C.success : "#92400E"} />
          </View>
          <View style={{ flex: 1 }}>
            <Text style={[styles.statusTitle, { color: isOnline ? "#065F46" : "#92400E" }]}>
              {isOnline ? "Servidor disponible" : "Sin conexión al servidor"}
            </Text>
            <Text style={[styles.statusSub, { color: isOnline ? "#065F46" : "#92400E" }]}>
              {isOnline
                ? "Puedes revisar y aprobar las operaciones pendientes."
                : "Las operaciones seguirán encolándose hasta recuperar la red."}
            </Text>
          </View>
        </View>

        <View style={styles.summaryRow}>
          <View style={styles.summaryCard}>
            <Text style={styles.sLabel}>PENDIENTES</Text>
            <Text style={styles.sValue}>{pending.length}</Text>
          </View>
          <View style={styles.summaryCard}>
            <Text style={styles.sLabel}>DISPOSITIVOS</Text>
            <Text style={styles.sValue}>{new Set(pending.map((p) => p.deviceId)).size}</Text>
          </View>
          <View style={styles.summaryCard}>
            <Text style={styles.sLabel}>CONFLICTOS</Text>
            <Text style={[styles.sValue, { color: pending.some((p) => !!p.note) ? C.warn : C.gray800 }]}>
              {pending.filter((p) => !!p.note).length}
            </Text>
          </View>
        </View>

        <View style={styles.sectionHead}>
          <Text style={styles.sectionTitle}>Cola de aprobación</Text>
          {pending.length > 0 && (
            <TouchableOpacity testID="approve-all-btn" onPress={approveAll}>
              <Text style={styles.linkTxt}>Aprobar todo</Text>
            </TouchableOpacity>
          )}
        </View>

        {pending.length === 0 ? (
          <View style={styles.empty}>
            <View style={styles.emptyIcon}>
              <Feather name="check" size={28} color={C.success} />
            </View>
            <Text style={styles.emptyTitle}>Todo sincronizado</Text>
            <Text style={styles.emptySub}>No hay operaciones pendientes por revisar.</Text>
          </View>
        ) : (
          pending.map((p) => (
            <SyncCard
              key={p.id}
              p={p}
              onApprove={() => onApprove(p.id, p.productName)}
              onReject={() => onReject(p.id, p.productName)}
            />
          ))
        )}
      </ScrollView>

      {toast && (
        <View style={styles.toast}>
          <Feather name="check-circle" size={16} color={C.white} />
          <Text style={styles.toastTxt}>{toast}</Text>
        </View>
      )}
    </View>
  );
}

function SyncCard({
  p,
  onApprove,
  onReject,
}: {
  p: PendingSync;
  onApprove: () => void;
  onReject: () => void;
}) {
  const isEntry = p.type === "entrada";
  return (
    <View style={styles.card} testID={`sync-item-${p.id}`}>
      <View style={styles.cardHead}>
        <View
          style={[
            styles.typeTag,
            { backgroundColor: isEntry ? C.successBg : C.errorBg },
          ]}
        >
          <Feather
            name={isEntry ? "arrow-down-left" : "arrow-up-right"}
            size={11}
            color={isEntry ? C.success : C.error}
          />
          <Text
            style={[
              styles.typeTagTxt,
              { color: isEntry ? C.success : C.error },
            ]}
          >
            {p.type}
          </Text>
        </View>
        <Text style={styles.cardQty}>
          {isEntry ? "+" : "−"}{p.quantity} u
        </Text>
      </View>

      <Text style={styles.cardName}>{p.productName}</Text>
      <Text style={styles.cardSku}>{p.sku}</Text>

      <View style={styles.metaBlock}>
        <View style={styles.metaLine}>
          <Feather name="user" size={12} color={C.gray500} />
          <Text style={styles.metaTxt}>Capturado por {p.capturedBy}</Text>
        </View>
        <View style={styles.metaLine}>
          <Feather name="clock" size={12} color={C.gray500} />
          <Text style={styles.metaTxt}>{p.capturedAt}</Text>
        </View>
        <View style={styles.metaLine}>
          <Feather name="smartphone" size={12} color={C.gray500} />
          <Text style={styles.metaTxt}>Dispositivo {p.deviceId}</Text>
        </View>
      </View>

      {p.note && (
        <View style={styles.noteBox}>
          <Feather name="alert-triangle" size={12} color="#92400E" />
          <Text style={styles.noteTxt}>{p.note}</Text>
        </View>
      )}

      <View style={styles.actions}>
        <TouchableOpacity
          testID={`sync-reject-${p.id}`}
          style={[styles.btn, { backgroundColor: C.white, borderWidth: 1, borderColor: C.border }]}
          onPress={onReject}
        >
          <Feather name="x" size={16} color={C.error} />
          <Text style={[styles.btnTxt, { color: C.error }]}>Rechazar</Text>
        </TouchableOpacity>
        <TouchableOpacity
          testID={`sync-approve-${p.id}`}
          style={[styles.btn, { backgroundColor: C.primary }]}
          onPress={onApprove}
        >
          <Feather name="upload-cloud" size={16} color={C.white} />
          <Text style={[styles.btnTxt, { color: C.white }]}>Aprobar y subir</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: C.bg },
  title: { fontSize: 24, fontWeight: "800", color: C.gray800, letterSpacing: -0.5 },
  sub: { fontSize: 12, color: C.gray500, marginTop: 2, marginBottom: 16 },
  status: { flexDirection: "row", alignItems: "center", gap: 12, padding: 14, borderRadius: 14, borderWidth: 1, marginBottom: 14 },
  statusIcon: { width: 40, height: 40, borderRadius: 12, alignItems: "center", justifyContent: "center" },
  statusTitle: { fontSize: 14, fontWeight: "700" },
  statusSub: { fontSize: 11, marginTop: 2, fontWeight: "500" },
  summaryRow: { flexDirection: "row", gap: 10, marginBottom: 6 },
  summaryCard: { flex: 1, backgroundColor: C.white, padding: 14, borderRadius: 14, borderWidth: 1, borderColor: C.border },
  sLabel: { fontSize: 10, fontWeight: "700", color: C.gray500, letterSpacing: 1 },
  sValue: { fontSize: 22, fontWeight: "800", color: C.gray800, marginTop: 6 },
  sectionHead: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", marginTop: 22, marginBottom: 12 },
  sectionTitle: { fontSize: 15, fontWeight: "700", color: C.gray800 },
  linkTxt: { color: C.primary, fontWeight: "700", fontSize: 13 },
  card: { backgroundColor: C.white, padding: 16, borderRadius: 16, borderWidth: 1, borderColor: C.border, marginBottom: 12 },
  cardHead: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", marginBottom: 10 },
  typeTag: { flexDirection: "row", alignItems: "center", gap: 4, paddingHorizontal: 8, paddingVertical: 4, borderRadius: 5 },
  typeTagTxt: { fontSize: 10, fontWeight: "800", textTransform: "uppercase", letterSpacing: 0.8 },
  cardQty: { fontSize: 20, fontWeight: "800", color: C.gray800 },
  cardName: { fontSize: 15, fontWeight: "700", color: C.gray800 },
  cardSku: { fontSize: 11, fontWeight: "700", color: C.primary, marginTop: 2, letterSpacing: 0.5 },
  metaBlock: { marginTop: 12, gap: 5 },
  metaLine: { flexDirection: "row", alignItems: "center", gap: 6 },
  metaTxt: { fontSize: 12, color: C.gray500 },
  noteBox: { flexDirection: "row", alignItems: "center", gap: 6, backgroundColor: C.warnBg, padding: 10, borderRadius: 8, marginTop: 10 },
  noteTxt: { flex: 1, color: "#92400E", fontSize: 11, fontWeight: "600" },
  actions: { flexDirection: "row", gap: 10, marginTop: 14 },
  btn: { flex: 1, height: 44, borderRadius: 12, flexDirection: "row", alignItems: "center", justifyContent: "center", gap: 6 },
  btnTxt: { fontSize: 13, fontWeight: "700" },
  empty: { alignItems: "center", padding: 40, backgroundColor: C.white, borderRadius: 16, borderWidth: 1, borderColor: C.border },
  emptyIcon: { width: 60, height: 60, borderRadius: 30, backgroundColor: C.successBg, alignItems: "center", justifyContent: "center", marginBottom: 12 },
  emptyTitle: { fontSize: 16, fontWeight: "700", color: C.gray800 },
  emptySub: { fontSize: 12, color: C.gray500, marginTop: 4, textAlign: "center" },
  toast: { position: "absolute", bottom: 26, left: 20, right: 20, flexDirection: "row", alignItems: "center", justifyContent: "center", gap: 8, backgroundColor: C.gray800, paddingVertical: 12, borderRadius: 12 },
  toastTxt: { color: C.white, fontSize: 13, fontWeight: "600" },
});
