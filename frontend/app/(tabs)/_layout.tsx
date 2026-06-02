import React from "react";
import { Tabs, Redirect } from "expo-router";
import { Feather } from "@expo/vector-icons";
import { useApp } from "../../context/AppContext";
import OfflineBanner from "../../components/OfflineBanner";
import { SafeAreaView } from "react-native-safe-area-context";

export default function TabsLayout() {
  const { user } = useApp();
  if (!user) return <Redirect href="/login" />;

  const isAdmin = user.role === "admin";

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: "#F8FAFC" }} edges={["top", "left", "right"]}>
      <OfflineBanner />
      <Tabs
        screenOptions={{
          headerShown: false,
          tabBarActiveTintColor: "#0F4C81",
          tabBarInactiveTintColor: "#64748B",
          tabBarStyle: {
            backgroundColor: "#FFFFFF",
            borderTopColor: "#E2E8F0",
            height: 66,
            paddingTop: 6,
            paddingBottom: 10,
          },
          tabBarLabelStyle: { fontSize: 11, fontWeight: "600" },
        }}
      >
        <Tabs.Screen
          name="dashboard"
          options={{
            title: "Inicio",
            tabBarIcon: ({ color, size }) => <Feather name="home" size={size - 2} color={color} />,
          }}
        />
        <Tabs.Screen
          name="inventory"
          options={{
            title: "Inventario",
            href: isAdmin ? "/(tabs)/inventory" : null,
            tabBarIcon: ({ color, size }) => <Feather name="package" size={size - 2} color={color} />,
          }}
        />
        <Tabs.Screen
          name="scanner"
          options={{
            title: "Escanear",
            tabBarIcon: ({ color }) => <Feather name="maximize" size={22} color={color} />,
          }}
        />
        <Tabs.Screen
          name="history"
          options={{
            title: "Historial",
            href: isAdmin ? "/(tabs)/history" : null,
            tabBarIcon: ({ color, size }) => <Feather name="list" size={size - 2} color={color} />,
          }}
        />
        <Tabs.Screen
          name="sync"
          options={{
            title: "Sync",
            href: isAdmin ? "/(tabs)/sync" : null,
            tabBarIcon: ({ color, size }) => <Feather name="refresh-cw" size={size - 2} color={color} />,
          }}
        />
      </Tabs>
    </SafeAreaView>
  );
}
