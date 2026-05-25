"""
Script to initialize the database with sample data
Run this after setting up PostgreSQL and configuring DATABASE_URL in .env
"""

from database import SessionLocal, engine, Base
from models import Product, User, StatusCheck
from passlib.context import CryptContext
import uuid
from datetime import datetime

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

def get_password_hash(password: str) -> str:
    return pwd_context.hash(password)

def init_db():
    # Create all tables
    Base.metadata.create_all(bind=engine)
    db = SessionLocal()
    
    try:
        # Check if data already exists
        existing_products = db.query(Product).count()
        if existing_products > 0:
            print("Database already initialized with data.")
            return
        
        # Create sample products
        sample_products = [
            Product(
                id=str(uuid.uuid4()),
                name="Casco de Seguridad",
                sku="EPP-001",
                category="EPP",
                quantity=50,
                minStock=10,
                location="Estante A1",
                price=25.00,
                description="Casco de seguridad industrial amarillo"
            ),
            Product(
                id=str(uuid.uuid4()),
                name="Guantes de Trabajo",
                sku="EPP-002",
                category="EPP",
                quantity=5,  # Low stock
                minStock=20,
                location="Estante A2",
                price=8.50,
                description="Guantes de nitrilo talla L"
            ),
            Product(
                id=str(uuid.uuid4()),
                name="Martillo",
                sku="HERR-001",
                category="Herramientas",
                quantity=15,
                minStock=5,
                location="Estante B1",
                price=12.99,
                description="Martillo de 16 oz"
            ),
            Product(
                id=str(uuid.uuid4()),
                name="Cable Eléctrico 50m",
                sku="ELEC-001",
                category="Eléctrico",
                quantity=8,
                minStock=3,
                location="Estante C1",
                price=45.00,
                description="Cable eléctrico 10 AWG 50 metros"
            ),
            Product(
                id=str(uuid.uuid4()),
                name="Lámpara LED Industrial",
                sku="ELEC-002",
                category="Eléctrico",
                quantity=0,  # Out of stock
                minStock=5,
                location="Estante C2",
                price=89.99,
                description="Lámpara LED 200W para bodega"
            ),
        ]
        
        # Add products to database
        for product in sample_products:
            product.lastUpdated = datetime.utcnow().isoformat()
            db.add(product)
        
        # Create sample users
        sample_user = User(
            id=str(uuid.uuid4()),
            email="admin@bodegaos.com",
            username="admin",
            password_hash=get_password_hash("admin1234"),
            role="admin",
            is_active=True
        )
        db.add(sample_user)

        sample_employee = User(
            id=str(uuid.uuid4()),
            email="empleado@bodegaos.com",
            username="empleado",
            password_hash=get_password_hash("empleado123"),
            role="user",
            is_active=True
        )
        db.add(sample_employee)
        
        # Create sample status check
        sample_status = StatusCheck(
            id=str(uuid.uuid4()),
            client_name="Mobile App",
            status="active"
        )
        db.add(sample_status)
        
        db.commit()
        print("✅ Database initialized successfully with sample data!")
        print(f"   - Created {len(sample_products)} sample products")
        print(f"   - Created 2 sample users (admin y empleado)")
        print(f"   - Created 1 status check record")
        
    except Exception as e:
        db.rollback()
        print(f"❌ Error initializing database: {e}")
    finally:
        db.close()

if __name__ == "__main__":
    init_db()
