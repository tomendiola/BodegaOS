from fastapi import FastAPI, APIRouter, Depends, HTTPException, status
from fastapi.responses import JSONResponse
from dotenv import load_dotenv
from starlette.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from pydantic import BaseModel, Field
from typing import List, Optional
import os
import logging
from pathlib import Path
from datetime import datetime
import uuid
from passlib.context import CryptContext

# Import database and models
from database import engine, SessionLocal, Base, get_db
from models import Product, User, Usuario, StatusCheck as StatusCheckModel, Inventory

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

def verify_password(plain_password: str, hashed_password: str) -> bool:
    return pwd_context.verify(plain_password, hashed_password)


def get_password_hash(password: str) -> str:
    return pwd_context.hash(password)

ROOT_DIR = Path(__file__).parent
load_dotenv(ROOT_DIR / '.env')

# Create tables
Base.metadata.create_all(bind=engine)

# Create the main app
app = FastAPI(title="BodegaOS API", version="1.0.0")

# Create a router with the /api prefix
api_router = APIRouter(prefix="/api", tags=["api"])

# CORS configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ===================== PYDANTIC MODELS =====================
class ProductCreate(BaseModel):
    name: str
    sku: str
    category: str
    quantity: int = 0
    minStock: int = 10
    location: Optional[str] = None
    price: Optional[float] = None
    description: Optional[str] = None

class ProductUpdate(BaseModel):
    name: Optional[str] = None
    quantity: Optional[int] = None
    minStock: Optional[int] = None
    location: Optional[str] = None
    price: Optional[float] = None
    description: Optional[str] = None

class ProductResponse(BaseModel):
    id: str
    name: str
    sku: str
    category: str
    quantity: int
    minStock: int
    location: Optional[str]
    price: Optional[float]
    description: Optional[str]
    lastUpdated: str
    
    class Config:
        from_attributes = True

class StatusCheckCreate(BaseModel):
    client_name: str

class StatusCheckResponse(BaseModel):
    id: str
    client_name: str
    status: str
    created_at: datetime
    
    class Config:
        from_attributes = True

class InventoryMovementCreate(BaseModel):
    product_id: str
    quantity_change: int
    movement_type: str
    reason: Optional[str] = None
    user_id: Optional[str] = None

class LoginRequest(BaseModel):
    usuario: str
    password: str

class UsuarioResponse(BaseModel):
    id: int
    usuario: str
    nombre: Optional[str]

    class Config:
        from_attributes = True

# ===================== HEALTH CHECK =====================
@api_router.get("/health")
async def health_check():
    return {"status": "ok", "message": "BodegaOS API is running"}

@api_router.post("/auth/login", response_model=UsuarioResponse)
async def login_user(login_request: LoginRequest, db: Session = Depends(get_db)):
    usuario = db.query(Usuario).filter(Usuario.usuario == login_request.usuario).first()
    if not usuario or usuario.contra != login_request.password:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Usuario o contraseña incorrectos",
            headers={"WWW-Authenticate": "Bearer"},
        )
    return usuario

# ===================== PRODUCTS ENDPOINTS =====================
@api_router.get("/products", response_model=List[ProductResponse])
async def get_products(
    db: Session = Depends(get_db),
    category: Optional[str] = None,
    skip: int = 0,
    limit: int = 100
):
    """Get all products with optional filtering"""
    query = db.query(Product)
    if category:
        query = query.filter(Product.category == category)
    return query.offset(skip).limit(limit).all()

@api_router.get("/products/{product_id}", response_model=ProductResponse)
async def get_product(product_id: str, db: Session = Depends(get_db)):
    """Get a specific product"""
    product = db.query(Product).filter(Product.id == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="Product not found")
    return product

@api_router.post("/products", response_model=ProductResponse)
async def create_product(
    product: ProductCreate,
    db: Session = Depends(get_db)
):
    """Create a new product"""
    db_product = Product(
        id=str(uuid.uuid4()),
        **product.dict(),
        lastUpdated=datetime.utcnow().isoformat()
    )
    db.add(db_product)
    db.commit()
    db.refresh(db_product)
    return db_product

@api_router.put("/products/{product_id}", response_model=ProductResponse)
async def update_product(
    product_id: str,
    product_update: ProductUpdate,
    db: Session = Depends(get_db)
):
    """Update a product"""
    db_product = db.query(Product).filter(Product.id == product_id).first()
    if not db_product:
        raise HTTPException(status_code=404, detail="Product not found")
    
    update_data = product_update.dict(exclude_unset=True)
    update_data["lastUpdated"] = datetime.utcnow().isoformat()
    
    for field, value in update_data.items():
        setattr(db_product, field, value)
    
    db.commit()
    db.refresh(db_product)
    return db_product

@api_router.delete("/products/{product_id}")
async def delete_product(product_id: str, db: Session = Depends(get_db)):
    """Delete a product"""
    db_product = db.query(Product).filter(Product.id == product_id).first()
    if not db_product:
        raise HTTPException(status_code=404, detail="Product not found")
    
    db.delete(db_product)
    db.commit()
    return {"message": "Product deleted successfully"}

# ===================== STATUS CHECK ENDPOINTS =====================
@api_router.post("/status", response_model=StatusCheckResponse)
async def create_status_check(
    status_check: StatusCheckCreate,
    db: Session = Depends(get_db)
):
    """Create a status check record"""
    db_status = StatusCheckModel(
        id=str(uuid.uuid4()),
        client_name=status_check.client_name,
        status="active"
    )
    db.add(db_status)
    db.commit()
    db.refresh(db_status)
    return db_status

@api_router.get("/status", response_model=List[StatusCheckResponse])
async def get_status_checks(db: Session = Depends(get_db)):
    """Get all status checks"""
    return db.query(StatusCheckModel).all()

# ===================== INVENTORY MOVEMENT ENDPOINTS =====================
@api_router.post("/inventory/movements")
async def create_inventory_movement(
    movement: InventoryMovementCreate,
    db: Session = Depends(get_db)
):
    """Record an inventory movement"""
    # Check if product exists
    product = db.query(Product).filter(Product.id == movement.product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="Product not found")
    
    # Create movement record
    db_movement = Inventory(
        id=str(uuid.uuid4()),
        **movement.dict()
    )
    
    # Update product quantity
    product.quantity += movement.quantity_change
    product.lastUpdated = datetime.utcnow().isoformat()
    
    db.add(db_movement)
    db.commit()
    db.refresh(db_movement)
    
    return {
        "message": "Inventory movement recorded",
        "movement": db_movement,
        "new_quantity": product.quantity
    }

@api_router.get("/inventory/movements/{product_id}")
async def get_product_movements(
    product_id: str,
    db: Session = Depends(get_db),
    skip: int = 0,
    limit: int = 50
):
    """Get inventory movements for a specific product"""
    movements = db.query(Inventory).filter(
        Inventory.product_id == product_id
    ).offset(skip).limit(limit).all()
    return movements

# ===================== ROOT ENDPOINT =====================
@api_router.get("/")
async def root():
    return {"message": "Welcome to BodegaOS API", "version": "1.0.0"}

# Include the router
app.include_router(api_router)

# Run the app
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "server:app",
        host="0.0.0.0",
        port=8000,
        reload=True
    )
