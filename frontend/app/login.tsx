import React, { useState } from "react";
import {
  View,
  Text,
  StyleSheet,
  TextInput,
  TouchableOpacity,
  KeyboardAvoidingView,
  Platform,
  ImageBackground,
  ScrollView,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons, Feather } from "@expo/vector-icons";
import { router } from "expo-router";
import { useApp, UserRole } from "../context/AppContext";

const COLORS = {
  primary: "#0F4C81",
  primaryDark: "#0B3960",
  white: "#FFFFFF",
  gray50: "#F8FAFC",
  gray200: "#E2E8F0",
  gray500: "#64748B",
  gray800: "#1E293B",
  error: "#DC2626",
};

const BG_URL =
  "https://images.unsplash.com/photo-1772300704502-410f0fbd43bb?crop=entropy&cs=srgb&fm=jpg&ixid=M3w3NDQ2NDN8MHwxfHNlYXJjaHwzfHxtb2Rlcm4lMjB3YXJlaG91c2UlMjBpbnRlcmlvcnxlbnwwfHx8fDE3Nzc0MDEwNzB8MA&ixlib=rb-4.1.0&q=85";

export default function Login() {
  const { login } = useApp();
  const [email, setEmail] = useState("admin@bodega.mx");
  const [password, setPassword] = useState("••••••••");
  const [role, setRole] = useState<UserRole>("admin");
  const [error, setError] = useState("");

  const handleLogin = () => {
    if (!email.trim() || !password.trim()) {
      setError("Ingresa tu correo y contraseña");
      return;
    }
    setError("");
    login(role);
    router.replace("/(tabs)/dashboard");
  };

  return (
    <ImageBackground source={{ uri: BG_URL }} style={styles.bg} resizeMode="cover">
      <View style={styles.overlay} />
      <SafeAreaView style={{ flex: 1 }} edges={["top", "bottom"]}>
        <KeyboardAvoidingView
          style={{ flex: 1 }}
          behavior={Platform.OS === "ios" ? "padding" : "height"}
        >
          <ScrollView
            contentContainerStyle={styles.scroll}
            keyboardShouldPersistTaps="handled"
            showsVerticalScrollIndicator={false}
          >
            <View style={styles.brandWrap}>
              <View style={styles.logoBox}>
                <Feather name="box" size={32} color={COLORS.white} />
              </View>
              <Text style={styles.brand}>Bodega<Text style={{ fontWeight: "400" }}>OS</Text></Text>
              <Text style={styles.brandSub}>Gestión de Inventario · QR / Código de Barras</Text>
            </View>

            <View style={styles.card} testID="login-card">
              <Text style={styles.title}>Iniciar sesión</Text>
              <Text style={styles.subtitle}>Accede con tu cuenta corporativa</Text>

              <Text style={styles.label}>Correo</Text>
              <View style={styles.inputWrap}>
                <Ionicons name="mail-outline" size={18} color={COLORS.gray500} />
                <TextInput
                  testID="login-email-input"
                  value={email}
                  onChangeText={setEmail}
                  placeholder="nombre@bodega.mx"
                  placeholderTextColor="#94A3B8"
                  autoCapitalize="none"
                  keyboardType="email-address"
                  style={styles.input}
                />
              </View>

              <Text style={styles.label}>Contraseña</Text>
              <View style={styles.inputWrap}>
                <Ionicons name="lock-closed-outline" size={18} color={COLORS.gray500} />
                <TextInput
                  testID="login-password-input"
                  value={password}
                  onChangeText={setPassword}
                  placeholder="••••••••"
                  placeholderTextColor="#94A3B8"
                  secureTextEntry
                  style={styles.input}
                />
              </View>

              <Text style={styles.label}>Rol de acceso</Text>
              <View style={styles.roleRow}>
                <TouchableOpacity
                  testID="login-role-employee"
                  onPress={() => setRole("empleado")}
                  style={[styles.roleBtn, role === "empleado" && styles.roleBtnActive]}
                >
                  <Feather
                    name="user"
                    size={16}
                    color={role === "empleado" ? COLORS.white : COLORS.primary}
                  />
                  <Text style={[styles.roleTxt, role === "empleado" && styles.roleTxtActive]}>
                    Empleado
                  </Text>
                </TouchableOpacity>
                <TouchableOpacity
                  testID="login-role-admin"
                  onPress={() => setRole("admin")}
                  style={[styles.roleBtn, role === "admin" && styles.roleBtnActive]}
                >
                  <Feather
                    name="shield"
                    size={16}
                    color={role === "admin" ? COLORS.white : COLORS.primary}
                  />
                  <Text style={[styles.roleTxt, role === "admin" && styles.roleTxtActive]}>
                    Administrador
                  </Text>
                </TouchableOpacity>
              </View>

              {error ? (
                <View style={styles.errorBox} testID="login-error">
                  <Feather name="alert-circle" size={14} color={COLORS.error} />
                  <Text style={styles.errorTxt}>{error}</Text>
                </View>
              ) : null}

              <TouchableOpacity
                testID="login-submit-button"
                style={styles.cta}
                onPress={handleLogin}
                activeOpacity={0.85}
              >
                <Text style={styles.ctaTxt}>Entrar</Text>
                <Feather name="arrow-right" size={18} color={COLORS.white} />
              </TouchableOpacity>

              <View style={styles.helperRow}>
                <Feather name="info" size={12} color={COLORS.gray500} />
                <Text style={styles.helper}>Prototipo visual · datos simulados</Text>
              </View>
            </View>

            <Text style={styles.footer}>
              v1.0 · Aranda Rico F. & Rico Mendiola A. · 2026
            </Text>
          </ScrollView>
        </KeyboardAvoidingView>
      </SafeAreaView>
    </ImageBackground>
  );
}

const styles = StyleSheet.create({
  bg: { flex: 1, backgroundColor: COLORS.primaryDark },
  overlay: { ...StyleSheet.absoluteFillObject, backgroundColor: "rgba(11,57,96,0.78)" },
  scroll: { paddingHorizontal: 24, paddingVertical: 32, flexGrow: 1, justifyContent: "center" },
  brandWrap: { alignItems: "flex-start", marginBottom: 32 },
  logoBox: {
    width: 56, height: 56, borderRadius: 14,
    backgroundColor: "rgba(255,255,255,0.12)",
    borderWidth: 1, borderColor: "rgba(255,255,255,0.25)",
    alignItems: "center", justifyContent: "center", marginBottom: 16,
  },
  brand: { color: COLORS.white, fontSize: 30, fontWeight: "800", letterSpacing: -0.5 },
  brandSub: { color: "rgba(255,255,255,0.75)", fontSize: 13, marginTop: 4 },
  card: {
    backgroundColor: COLORS.white, borderRadius: 20, padding: 24,
    shadowColor: "#000", shadowOpacity: 0.15, shadowRadius: 20, shadowOffset: { width: 0, height: 10 }, elevation: 8,
  },
  title: { fontSize: 22, fontWeight: "700", color: COLORS.gray800 },
  subtitle: { fontSize: 13, color: COLORS.gray500, marginTop: 4, marginBottom: 20 },
  label: { fontSize: 11, fontWeight: "700", color: COLORS.gray500, letterSpacing: 1, marginTop: 10, marginBottom: 6, textTransform: "uppercase" },
  inputWrap: {
    flexDirection: "row", alignItems: "center", gap: 10,
    borderWidth: 1, borderColor: COLORS.gray200, borderRadius: 10,
    paddingHorizontal: 12, height: 48, backgroundColor: COLORS.gray50,
  },
  input: { flex: 1, fontSize: 15, color: COLORS.gray800 },
  roleRow: { flexDirection: "row", gap: 10, marginTop: 4 },
  roleBtn: {
    flex: 1, flexDirection: "row", alignItems: "center", justifyContent: "center", gap: 8,
    borderWidth: 1.5, borderColor: COLORS.primary, borderRadius: 10, height: 44,
    backgroundColor: COLORS.white,
  },
  roleBtnActive: { backgroundColor: COLORS.primary },
  roleTxt: { color: COLORS.primary, fontWeight: "600", fontSize: 14 },
  roleTxtActive: { color: COLORS.white },
  errorBox: { flexDirection: "row", alignItems: "center", gap: 6, marginTop: 12, backgroundColor: "#FEE2E2", padding: 10, borderRadius: 8 },
  errorTxt: { color: COLORS.error, fontSize: 13, fontWeight: "500" },
  cta: {
    marginTop: 20, backgroundColor: COLORS.primary, height: 52, borderRadius: 12,
    flexDirection: "row", alignItems: "center", justifyContent: "center", gap: 8,
  },
  ctaTxt: { color: COLORS.white, fontSize: 16, fontWeight: "700" },
  helperRow: { flexDirection: "row", alignItems: "center", gap: 6, justifyContent: "center", marginTop: 14 },
  helper: { fontSize: 12, color: COLORS.gray500 },
  footer: { color: "rgba(255,255,255,0.55)", fontSize: 11, textAlign: "center", marginTop: 24 },
});
