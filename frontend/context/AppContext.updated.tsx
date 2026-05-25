/**
 * Updated AppContext with PostgreSQL integration
 * Replace your existing AppContext.tsx with this version to use the real API
 */

import React, { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { ProductService, StatusService } from "../services/api";

export interface Product {
  id: string;
  name: string;
  sku: string;
  category: string;
  quantity: number;
  minStock: number;
  location: string;
  price?: number;
  description?: string;
  lastUpdated: string;
}

interface AppContextType {
  products: Product[];
  loading: boolean;
  error: string | null;
  refreshProducts: () => Promise<void>;
  addProduct: (product: Omit<Product, "id" | "lastUpdated">) => Promise<void>;
  updateProduct: (id: string, updates: Partial<Product>) => Promise<void>;
  deleteProduct: (id: string) => Promise<void>;
  getProductsByCategory: (category: string) => Product[];
  getLowStockProducts: () => Product[];
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const AppProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Load products from API
  const refreshProducts = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await ProductService.getAll();
      setProducts(data);
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : "Failed to load products";
      setError(errorMsg);
      console.error("Error loading products:", err);
    } finally {
      setLoading(false);
    }
  };

  // Add new product
  const addProduct = async (product: Omit<Product, "id" | "lastUpdated">) => {
    try {
      setError(null);
      const newProduct = await ProductService.create(product);
      setProducts([...products, newProduct]);
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : "Failed to add product";
      setError(errorMsg);
      console.error("Error adding product:", err);
      throw err;
    }
  };

  // Update product
  const updateProduct = async (id: string, updates: Partial<Product>) => {
    try {
      setError(null);
      const updatedProduct = await ProductService.update(id, updates);
      setProducts(
        products.map((p) => (p.id === id ? updatedProduct : p))
      );
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : "Failed to update product";
      setError(errorMsg);
      console.error("Error updating product:", err);
      throw err;
    }
  };

  // Delete product
  const deleteProduct = async (id: string) => {
    try {
      setError(null);
      await ProductService.delete(id);
      setProducts(products.filter((p) => p.id !== id));
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : "Failed to delete product";
      setError(errorMsg);
      console.error("Error deleting product:", err);
      throw err;
    }
  };

  // Filter products by category
  const getProductsByCategory = (category: string) => {
    return products.filter((p) => p.category === category);
  };

  // Get low stock products
  const getLowStockProducts = () => {
    return products.filter((p) => p.quantity < p.minStock);
  };

  // Load products on mount
  useEffect(() => {
    refreshProducts();
  }, []);

  const value: AppContextType = {
    products,
    loading,
    error,
    refreshProducts,
    addProduct,
    updateProduct,
    deleteProduct,
    getProductsByCategory,
    getLowStockProducts,
  };

  return (
    <AppContext.Provider value={value}>
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error("useApp must be used within AppProvider");
  }
  return context;
};
