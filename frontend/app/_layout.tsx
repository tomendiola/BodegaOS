import React, { useEffect } from "react";
import { Stack } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { AppProvider } from "../context/AppContext";
import * as SplashScreen from 'expo-splash-screen';

// Evitar que la splash screen se oculte automáticamente hasta que estemos listos
SplashScreen.preventAutoHideAsync().catch(() => {});

export default function RootLayout() {
  useEffect(() => {
    // Forzar que se oculte después de un pequeño delay para asegurar el render
    setTimeout(async () => {
      await SplashScreen.hideAsync().catch(() => {});
    }, 500);
  }, []);

  return (
    <SafeAreaProvider>
      <AppProvider>
        <StatusBar style="dark" />
        <Stack screenOptions={{ headerShown: false, contentStyle: { backgroundColor: "#F8FAFC" } }}>
          <Stack.Screen name="index" />
          <Stack.Screen name="login" />
          <Stack.Screen name="(tabs)" />
          <Stack.Screen name="product/[id]" options={{ presentation: "card" }} />
        </Stack>
      </AppProvider>
    </SafeAreaProvider>
  );
}
