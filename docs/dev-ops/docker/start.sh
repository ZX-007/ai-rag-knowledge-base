docker run -d ^
  --name ai-rag-knowledge-base ^
  -e SPRING_PROFILES_ACTIVE=local ^
  -p 8080:8080 ^
  ai-rag-knowledge-base:v1.0.0