import os

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import Session
from sqlalchemy.pool import StaticPool

from app.database import Base, get_session
from app.main import app


@pytest.fixture
def client():
    # Optional PostgreSQL URL must point at a dedicated, migrated test database.
    url = os.getenv("TEST_DATABASE_URL")
    engine = (
        create_engine(url)
        if url
        else create_engine(
            "sqlite://", connect_args={"check_same_thread": False}, poolclass=StaticPool
        )
    )
    if not url:
        Base.metadata.create_all(engine)
    with engine.connect() as connection:
        transaction = connection.begin()
        with Session(bind=connection, join_transaction_mode="create_savepoint") as session:
            app.dependency_overrides[get_session] = lambda: session
            try:
                with TestClient(app) as test_client:
                    yield test_client
            finally:
                app.dependency_overrides.clear()
        transaction.rollback()
    engine.dispose()
