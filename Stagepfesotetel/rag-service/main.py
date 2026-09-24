import os
import time
import google.generativeai as genai
import chromadb
from chromadb.utils import embedding_functions
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from dotenv import load_dotenv

load_dotenv()

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
if not GEMINI_API_KEY:
    raise ValueError("GEMINI_API_KEY missing in .env file")

genai.configure(api_key=GEMINI_API_KEY)
llm = genai.GenerativeModel("gemini-3.6-flash")
print("📥 Loading embedding model...")
embedder = embedding_functions.DefaultEmbeddingFunction()
print("✅ Embedding model loaded")

chroma_client = chromadb.PersistentClient(path="./chroma_db")
collection = chroma_client.get_or_create_collection(name="docs")


def load_and_index_documents():
    doc_folder = "./documents"
    if not os.path.exists(doc_folder):
        print("⚠️ documents/ folder not found")
        return

    chunks = []
    ids = []
    for filename in os.listdir(doc_folder):
        if filename.endswith(".txt"):
            filepath = os.path.join(doc_folder, filename)
            with open(filepath, "r", encoding="utf-8") as f:
                content = f.read()
                for i, para in enumerate(content.split("\n\n")):
                    if para.strip():
                        chunks.append(para.strip())
                        ids.append(f"{filename}_{i}")

    if not chunks:
        print("⚠️ No documents found")
        return

    existing = collection.count()
    if existing >= len(chunks):
        print(f"✅ Already indexed {existing} chunks")
        return

    print(f"📚 Indexing {len(chunks)} chunks...")
    embeddings = embedder(chunks)
    collection.add(documents=chunks, embeddings=embeddings, ids=ids)
    print(f"✅ Indexed {len(chunks)} chunks")


app = FastAPI()


@app.on_event("startup")
def startup():
    load_and_index_documents()


class Question(BaseModel):
    question: str


@app.post("/chat")
def chat(q: Question):
    start = time.time()
    try:
        query_embedding = embedder([q.question])[0]
        results = collection.query(query_embeddings=[query_embedding], n_results=5)
        retrieved_chunks = results["documents"][0]
        sources = results["ids"][0]

        context = "\n\n---\n\n".join(retrieved_chunks)
        prompt = f"""Tu es un assistant technique pour des techniciens en fibre optique.
Réponds en français, de façon claire et concise.
Utilise UNIQUEMENT les informations du contexte ci-dessous.
Si les informations ne sont que partiellement présentes, réponds avec ce que tu trouves et indique les limites.

=== CONTEXTE ===
{context}

=== QUESTION ===
{q.question}

=== RÉPONSE ==="""

        response = llm.generate_content(prompt)
        answer = response.text

        elapsed = round(time.time() - start, 2)
        print(f"✅ [{elapsed}s] Answered: {q.question[:60]}...")

        return {"answer": answer, "sources": sources}

    except Exception as e:
        print(f"❌ Error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/health")
def health():
    return {"status": "ok", "chunks_indexed": collection.count()}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)