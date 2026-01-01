from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer

app = FastAPI(title="Embeddings Service")
model = SentenceTransformer("sentence-transformers/all-MiniLM-L6-v2")

class EmbedRequest(BaseModel):
    texts: list[str]

class EmbedResponse(BaseModel):
    vectors: list[list[float]]

@app.post("/embed", response_model=EmbedResponse)
def embed(req: EmbedRequest):
    vectors = model.encode(req.texts, normalize_embeddings=True).tolist()
    return {"vectors": vectors}
