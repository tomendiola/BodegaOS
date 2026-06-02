/**
 * API Service for connecting to BodegaOS backend
 * Configure the API_URL to match your backend server
 */

// Change this to your backend URL
const API_URL = process.env.EXPO_PUBLIC_API_URL || 'http://10.0.2.2:8000/api';

// Generic fetch wrapper
async function apiRequest(endpoint: string, options: RequestInit = {}) {
  try {
    const response = await fetch(`${API_URL}${endpoint}`, {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    });

    if (!response.ok) {
      throw new Error(`API Error: ${response.status} ${response.statusText}`);
    }

    return await response.json();
  } catch (error) {
    console.error(`API Request Failed [${endpoint}]:`, error);
    throw error;
  }
}

// ===================== PRODUCTS =====================
export const ProductService = {
  // Get all products
  async getAll(category?: string) {
    const params = new URLSearchParams();
    if (category) params.append('category', category);
    return apiRequest(`/products?${params.toString()}`);
  },

  // Get single product
  async getById(id: string) {
    return apiRequest(`/products/${id}`);
  },

  // Create product
  async create(product: {
    name: string;
    sku: string;
    category: string;
    quantity?: number;
    minStock?: number;
    location?: string;
    price?: number;
    description?: string;
  }) {
    return apiRequest('/products', {
      method: 'POST',
      body: JSON.stringify(product),
    });
  },

  // Update product
  async update(id: string, updates: Partial<any>) {
    return apiRequest(`/products/${id}`, {
      method: 'PUT',
      body: JSON.stringify(updates),
    });
  },

  // Delete product
  async delete(id: string) {
    return apiRequest(`/products/${id}`, {
      method: 'DELETE',
    });
  },
};

// ===================== AUTHENTICATION =====================
export const AuthService = {
  async login(usuario: string, password: string) {
    return apiRequest('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ usuario, password }),
    });
  },
};

// ===================== INVENTORY MOVEMENTS =====================
export const InventoryService = {
  // Record an inventory movement
  async recordMovement(movement: {
    product_id: string;
    quantity_change: number;
    movement_type: 'entrada' | 'salida' | 'ajuste';
    reason?: string;
    user_id?: string;
  }) {
    return apiRequest('/inventory/movements', {
      method: 'POST',
      body: JSON.stringify(movement),
    });
  },

  // Get movements for a product
  async getProductMovements(productId: string) {
    return apiRequest(`/inventory/movements/${productId}`);
  },
};

// ===================== STATUS CHECK =====================
export const StatusService = {
  // Check API health
  async checkHealth() {
    return apiRequest('/health');
  },

  // Get all status checks
  async getAll() {
    return apiRequest('/status');
  },

  // Create status check
  async create(clientName: string) {
    return apiRequest('/status', {
      method: 'POST',
      body: JSON.stringify({ client_name: clientName }),
    });
  },
};

// ===================== UTILITY =====================
export const setAPIUrl = (url: string) => {
  // This allows dynamic URL changes if needed
  console.log('API URL set to:', url);
};

export const getAPIUrl = () => API_URL;
