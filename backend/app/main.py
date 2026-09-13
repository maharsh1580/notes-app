from fastapi import FastAPI

from app.routes import router

app = FastAPI(title="Notes API", version="0.1.0")
app.include_router(router)


@app.get("/health", tags=["health"])
def health():
    return {"status": "ok"}
