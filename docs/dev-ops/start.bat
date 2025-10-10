docker build -t white0618/ai-knowledge-base:v1.1.0 .
docker login -u ${username} -p ${password}
docker push white0618/ai-knowledge-base:v1.1.0
docker save white0618/ai-knowledge-base:v1.1.0 -o ./ai-knowledge-base.tar
docker load -i ./ai-knowledge-base.tar
